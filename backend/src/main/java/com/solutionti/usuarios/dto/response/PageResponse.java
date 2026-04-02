package com.solutionti.usuarios.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Contrato de paginação estável exposto pela API.
 * <p>
 * Expõe apenas os campos que formam parte do contrato público, removendo
 * internals do Spring (pageable, sort, etc.) que não devem ser consumidos
 * por clientes externos. O formato é compatível com a interface
 * {@code PaginatedResponse<T>} do frontend.
 */
@Schema(description = "Resposta paginada com metadados de navegação")
public record PageResponse<T>(

    @Schema(description = "Conteúdo da página atual")
    List<T> content,

    @Schema(description = "Número da página atual (zero-based)", example = "0")
    int number,

    @Schema(description = "Tamanho máximo de itens por página", example = "20")
    int size,

    @Schema(description = "Total de elementos em todas as páginas")
    long totalElements,

    @Schema(description = "Total de páginas disponíveis")
    int totalPages,

    @Schema(description = "Indica se é a primeira página")
    boolean first,

    @Schema(description = "Indica se é a última página")
    boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}
