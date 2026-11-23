package com.findwork.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.findwork.entities.Categoria;
import com.findwork.repositories.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTests {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setup() {
        categoria = Categoria.builder()
                .idCategoria(1)
                .nombre("Tecnología")
                .descripcion("Ofertas de tecnología")
                .build();
    }

    @DisplayName("Test para guardar una categoría")
    @Test
    void testGuardarCategoria() {
        // given
        given(categoriaRepository.save(categoria)).willReturn(categoria);

        // when
        Categoria categoriaGuardada = categoriaService.save(categoria);

        // then
        assertThat(categoriaGuardada).isNotNull();
    }

    @DisplayName("Test para listar las categorías")
    @Test
    void testListarCategoria() {
        // given
        Categoria categoria1 = Categoria.builder()
                .idCategoria(2)
                .nombre("Diseño")
                .descripcion("Ofertas de diseño")
                .build();

        given(categoriaRepository.findAll()).willReturn(List.of(categoria, categoria1));

        // when
        List<Categoria> categorias = categoriaService.findAll();

        // then
        assertThat(categorias).isNotNull();
        assertThat(categorias.size()).isEqualTo(2);
    }


    @DisplayName("Test para obtener una categoría por ID")
    @Test
    void testObtenerCategoriaPorId() {
        // given
        given(categoriaRepository.findById(1)).willReturn(Optional.of(categoria));

        // when
        Categoria categoriaGuardada = categoriaService.findById(1);

        // then
        assertThat(categoriaGuardada).isNotNull();
        assertThat(categoriaGuardada.getIdCategoria()).isEqualTo(1);
    }

    @DisplayName("Test para actualizar una categoría")
    @Test
    void testActualizarCategoria() {
        // given
        Categoria datos = Categoria.builder()
                .nombre("Diseño")
                .descripcion("Nueva descripción")
                .build();

        given(categoriaRepository.findById(1)).willReturn(Optional.of(categoria));
        given(categoriaRepository.save(categoria)).willReturn(categoria);

        // when
        Categoria categoriaActualizada = categoriaService.update(1, datos);

        // then
        assertThat(categoriaActualizada.getNombre()).isEqualTo("Diseño");
        assertThat(categoriaActualizada.getDescripcion()).isEqualTo("Nueva descripción");
    }

    @DisplayName("Test para eliminar una categoría")
    @Test
    void testEliminarCategoria() {
        // given
        Integer categoriaId = 1;
        willDoNothing().given(categoriaRepository).deleteById(categoriaId);

        // when
        categoriaService.delete(categoriaId);

        // then
        verify(categoriaRepository, times(1)).deleteById(categoriaId);
    }
}




