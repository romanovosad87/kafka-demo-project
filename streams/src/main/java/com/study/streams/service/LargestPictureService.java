package com.study.streams.service;


import com.study.avromodels.Message;
import com.study.avromodels.Picture;
import java.util.Optional;

public interface LargestPictureService {

    Optional<Picture> getLargestPicture(Message message);

}
