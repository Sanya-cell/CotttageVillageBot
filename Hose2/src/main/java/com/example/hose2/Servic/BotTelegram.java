package com.example.hose2.Servic;

import com.example.hose2.Config.Config;
import com.example.hose2.Model.Vehicle;
import com.example.hose2.Model.VehicleTemporary;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
@Component
@AllArgsConstructor
public class BotTelegram extends TelegramLongPollingBot {

    private  final List<Vehicle> vehicles;
    private  final List<VehicleTemporary>vehicleTemporaries;
    final Config config;
    private final Map<Long, Consumer<Update>> chatCallbacks = new HashMap<>();
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
    @Override
    public String getBotToken() {
        return config.getToken();
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            Consumer<Update> callback = chatCallbacks.get(chatId);

            if (callback != null) {
                callback.accept(update);
                chatCallbacks.remove(chatId); // Удаляем callback после вызова
            }
            if (messageText.equals("/restart")) {
                StartBot(chatId);
            } else if (messageText.equals("/register")) {
                StartCommandReceived(chatId, "");
                processVehicleRegistration(chatId, "");
            } else if (messageText.equals("/temporary_access")) {
                StartTemporaryAccessReceived(chatId, "");
                processTemporaryAccessRegistration(chatId, "");}
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("/register")) {
                StartCommandReceived(chatId, "/register");
                processVehicleRegistration(chatId, "/register");
            } else if (callbackData.equals("/temporary_access")) {
                StartTemporaryAccessReceived(chatId, "/temporary_access");
                processTemporaryAccessRegistration(chatId, "temporary_access");
            }
        }
    }
    private void StartBot(long chatId) {
        String answer = " Выберите команду + /restart";

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();


        InlineKeyboardButton registerButton = new InlineKeyboardButton();
        registerButton.setText("Регистрация постоянного пропуска");
        registerButton.setCallbackData("/register");
        rowInline.add(registerButton);

        InlineKeyboardButton temporaryAccessButton = new InlineKeyboardButton();
        temporaryAccessButton.setText("Регистрация временного пропуска");
        temporaryAccessButton.setCallbackData("/temporary_access");
        rowInline.add(temporaryAccessButton);

        rowsInline.add(rowInline);
        inlineKeyboardMarkup.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(answer);
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
    private void StartCommandReceived(long chatId, String name) {
        String answer = " Для регистрации постоянного транспортного средства введите следующую информацию:+ /restart  \n" +
                "1. Гос номер авто\n" +
                "2. Модель и цвет авто\n" +
                "3. Адрес собственника\n" +
                "Все данные вводите через запятую. Например: А123ВС, Тойота Камри, белый, г. Москва, ул. Ленина, д. 1";
        sendMassage(chatId, answer);
        // Ожидаем сообщение от пользователя и обрабатываем его
        CompletableFuture<String> future = waitForUserInput(chatId, update -> {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String userInput = update.getMessage().getText();
                processVehicleRegistration(chatId, userInput);
            }
        });
        // Добавляем обработку исключений, которые могут возникнуть при получении сообщения
        future.exceptionally(ex -> {
            log.error("Ошибка при получении сообщения от пользователя", ex);
            return null;
        });
    }
    private void StartTemporaryAccessReceived(long chatId, String name) {
        String answer = "Для регистрации временного доступа введите следующую информацию: + /restart  \n" +
                "1. Гос номер авто\n" +
                "2. Модель и цвет авто\n" +
                "3. Срок действия (в течение дня или дата окончания в формате ГГГГ-ММ-ДД)\n" +
                "4. Адрес куда приедет\n" +
                "Все данные вводите через запятую. Например: А123ВС, Тойота Камри, белый, сегодня, г. Москва, ул. Ленина, д. 1";
        sendMassage(chatId, answer);
        // Ожидаем сообщение от пользователя и обрабатываем его
        CompletableFuture<String> future = waitForUserInput(chatId, update -> {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String userInput = update.getMessage().getText();
                processTemporaryAccessRegistration(chatId, userInput);
            }
        });
        // Добавляем обработку исключений, которые могут возникнуть при получении сообщения
        future.exceptionally(ex -> {
            log.error("Ошибка при получении сообщения от пользователя", ex);
            return null;
        });
    }
    // Метод для ожидания сообщения от пользователя
    private CompletableFuture<String> waitForUserInput(long chatId, Consumer<Update> callback) {
        CompletableFuture<String> future = new CompletableFuture<>();
        executeLater(chatId, callback);
        return future;
    }
    private void executeLater(long chatId, Consumer<Update> callback) {
        chatCallbacks.put(chatId, callback);
    }
    private void processTemporaryAccessRegistration(long chatId, String vehicleInfo) {
        String[] parts = vehicleInfo.split(",");
        if (parts.length == 7) { // Теперь ожидается 6 частей данных
            String licensePlateTemporary = parts[0].trim();
            String modelAndColorTemporary = parts[1].trim();
            String validityPeriodTemporary = parts[2].trim();
            String dateTemporary = parts[3].trim();
            String cityTemporary = parts[4].trim();
            String streetTemporary = parts[5].trim();
            String homeTemporary = parts[6].trim();

            // Проверка на пустые значения
            if (licensePlateTemporary.isEmpty() || modelAndColorTemporary.isEmpty() || validityPeriodTemporary.isEmpty() || cityTemporary.isEmpty() || streetTemporary.isEmpty() || homeTemporary.isEmpty()) {
                sendMassage(chatId, "Все поля должны быть заполнены. Попробуйте еще раз");
                return;
            }
            // Разделение модели и цвета, если они введены вместе
            String[] modelColorParts = modelAndColorTemporary.split(" ");
            if (modelColorParts.length != 2) {
                sendMassage(chatId, "Неверный формат данных для модели и цвета. Попробуйте еще раз");
                return;
            }
            String model = modelColorParts[0];
            String color = modelColorParts[1];

            // Проверка формата срока действия
            if (!isValidDate(dateTemporary)) {
                sendMassage(chatId, "Неверный формат срока действия. Ожидается ГГГГ-ММ-ДД или 'сегодня'");
                return;
            }
            VehicleTemporary vehicleTemporary = new VehicleTemporary(licensePlateTemporary, model, color, validityPeriodTemporary,
                    dateTemporary, cityTemporary, streetTemporary, homeTemporary);
            vehicleTemporaries.add(vehicleTemporary);
            sendMassage(chatId, "Временный доступ успешно зарегистрирован");
        }
    }
    private boolean isValidDate(String date) {
        // Регулярное выражение для проверки формата ГГГГ-ММ-ДД
        String dateFormat = "\\d{4}-\\d{2}-\\d{2}";
        // Проверка на "сегодня"
        if ("сегодня".equalsIgnoreCase(date)) {
            return true;
        }
        if (!date.matches(dateFormat)) {
            return false;
        }
        // Разбиваем строку на год, месяц и день
        String[] dateParts = date.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);

        // Проверяем, что месяц и день находятся в допустимых пределах
        if (month < 1 || month > 12) {
            return false;
        }
        if (day < 1 || day > 31) {
            return false;
        }
        // проверяем, что в феврале не больше 29 дней (для високосных лет)
        if (month == 2) {
            if (isLeapYear(year)) {
                if (day > 29) {
                    return false;
                }
            } else {
                if (day > 28) {
                    return false;
                }
            }
        }
        // Проверяем, что в апреле, июне, сентябре и ноябре не больше 30 дней
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            if (day > 30) {
                return false;
            }
        }
        return true;
    }
    private boolean isLeapYear(int year) {
        // Проверка, является ли год високосным
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
    private void processVehicleRegistration(long chatId, String vehicleInfo) {
        String[] parts = vehicleInfo.split(",");
        if (parts.length == 6) { // Теперь ожидается 6 частей данных
            String licensePlate =  parts[0].trim();
            String modelAndColor = parts[1].trim();
            String city = parts[2].trim();
            String street = parts[3].trim();
            String nameModel = parts[4].trim();
            String home = parts[5].trim();

            // Проверка на пустые значения
            if (licensePlate.isEmpty() || modelAndColor.isEmpty() || city.isEmpty() || street.isEmpty() || nameModel.isEmpty() || home.isEmpty()) {
                sendMassage(chatId, "Все поля должны быть заполнены. Попробуйте еще раз");
                return;
            }
            // Разделение модели и цвета, если они введены вместе
            String[] modelColorParts = modelAndColor.split(" ");
            if (modelColorParts.length != 2) {
                sendMassage(chatId, "Неверный формат данных для модели и цвета. Попробуйте еще раз");
                return;
            }
            String model = modelColorParts[0];
            String color = modelColorParts[1];
            Vehicle vehicle = new Vehicle(licensePlate, model, color, city, street, nameModel, home);
            vehicles.add(vehicle);
            sendMassage(chatId, "Автомобиль успешно зарегистрирован");
        }
    }
    private boolean isPreviousMessageTemporaryAccessInstructions(long chatId) {
        // Здесь Вам нужно реализовать логику, которая будет проверять, было ли предыдущее сообщение с инструкциями для регистрации временного доступа
        // Это может быть проверка в Вашей базе данных или в памяти приложения, в зависимости от Вашей архитектуры
        // Например, Вы можете хранить последнее сообщение для каждого чата в памяти приложения
        // Заглушка для примера
        return false;
    }
    private void sendMassage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId)); // Исправлена опечатка в имени переменной
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage()); // Использование log.error вместо log.info для ошибок
        }
    }
}
