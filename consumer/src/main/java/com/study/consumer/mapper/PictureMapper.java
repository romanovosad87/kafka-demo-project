package com.study.consumer.mapper;

import com.study.avromodels.Picture;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PictureMapper {

    PictureDto pictureToDto(Picture picture);

}
