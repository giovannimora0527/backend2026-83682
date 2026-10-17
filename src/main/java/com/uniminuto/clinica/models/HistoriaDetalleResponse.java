package com.uniminuto.clinica.models;

import java.util.List;

public record HistoriaDetalleResponse(HistoriaResponse historia, PageResponse<AnotacionResponse> anotaciones) { }
