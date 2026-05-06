package com.hotellunara.common.hotel;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelInfoResponse {

    private String nombre;
    private int estrellas;
    private int pisos;
    private int totalHabitaciones;
    private String descripcion;
    private String direccion;
    private String telefono;
    private String email;
    private String horarioCheckIn;
    private String horarioCheckOut;
    private String politicaCancelacion;
    private List<String> imagenes;
    private Map<String, String> horariosRestaurante;
    private List<String> serviciosDestacados;
    private Map<String, Long> habitacionesPorTipo;
}
