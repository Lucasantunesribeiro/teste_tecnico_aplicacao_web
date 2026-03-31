package com.solutionti.usuarios.mapper;

import com.solutionti.usuarios.dto.request.EnderecoRequest;
import com.solutionti.usuarios.dto.response.EnderecoResponse;
import com.solutionti.usuarios.entity.Endereco;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EnderecoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "logradouro", ignore = true)
    @Mapping(target = "bairro", ignore = true)
    @Mapping(target = "cidade", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Endereco toEntity(EnderecoRequest request);

    @Mapping(target = "usuarioId", source = "usuario.id")
    EnderecoResponse toResponse(Endereco entity);

    List<EnderecoResponse> toResponseList(List<Endereco> entities);
}
