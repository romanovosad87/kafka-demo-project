package com.study.connector.mapper;


import com.study.connector.model.PictureEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PictureMapper {

    PictureDto pictureToDto(PictureEntity pictureEntity);
}
