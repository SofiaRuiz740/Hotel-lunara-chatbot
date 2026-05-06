package com.hotellunara.common.hotel;

import com.hotellunara.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotel")
@RequiredArgsConstructor
@Tag(name = "Hotel", description = "Informacion publica del hotel")
public class HotelInfoController {

    private final HotelInfoService hotelInfoService;

    @GetMapping("/info")
    @Operation(summary = "Obtener informacion publica del hotel")
    public ResponseEntity<ApiResponse<HotelInfoResponse>> getHotelInfo() {
        return ResponseEntity.ok(ApiResponse.success("Informacion del hotel obtenida correctamente",
                hotelInfoService.getHotelInfo()));
    }
}
