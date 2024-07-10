package com.example.hose2.Model;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehicleTemporary {


    private  Long id;

    private String licensePlateTemporary;
    private String nameModelTemporary;
    private String modelTemporary;
    private String colorTemporary;
    private String cityTemporary;
    private String streetTemporary;
    private String homeTemporary;
    private String dateTemporary;

    public VehicleTemporary(String licensePlateTemporary, String nameModelTemporary, String modelTemporary, String colorTemporary,
                            String cityTemporary, String streetTemporary, String homeTemporary, String dateTemporary) {
        this.licensePlateTemporary = licensePlateTemporary;
        this.nameModelTemporary = nameModelTemporary;
        this.modelTemporary = modelTemporary;
        this.colorTemporary = colorTemporary;
        this.cityTemporary = cityTemporary;
        this.streetTemporary = streetTemporary;
        this.homeTemporary = homeTemporary;
        this.dateTemporary = dateTemporary;
    }
}
