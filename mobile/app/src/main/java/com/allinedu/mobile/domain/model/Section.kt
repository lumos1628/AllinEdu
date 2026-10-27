package com.allinedu.mobile.domain.model

// Entidad de dominio: una sección de un grado en un año escolar.
// No sabe nada de Android ni del backend.
data class Section(
    val id: Long,
    val nombre: String,
    val grado: String,
    val descripcion: String,
)