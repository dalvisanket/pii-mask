package com.fetch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fetch.ds.entity.UserLogin;
import com.fetch.ds.repository.UserLoginRepository;
import com.fetch.util.EncoderUtil;
import com.fetch.util.exception.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;


import java.util.Date;


@Service
public class ListenerService {

    private static final Logger logger = LoggerFactory.getLogger(ListenerService.class);

    @Autowired
    private UserLoginRepository userLoginRepository;

    @Autowired
    private EncoderUtil encoderUtil;

    @SqsListener("${cloud.aws.end-point.uri}")
    public void loadMessageFromSQS(String message){

        try {
            ObjectMapper mapper = new ObjectMapper();
            UserLogin userLogin = mapper.readValue(message, UserLogin.class);

            userLogin.setMaskedIp(encoderUtil.maskValue(userLogin.getMaskedIp()));
            userLogin.setMaskedDeviceId(encoderUtil.maskValue(userLogin.getMaskedDeviceId()));
            userLogin.setCreateDate(new Date());

            userLoginRepository.save(userLogin);
        }
        catch (JsonProcessingException e){
            logger.error("ERROR Processinf Json : " + e.getMessage());
        }
        catch (ParsingException e){
            logger.error("ERROR masking fields : " + e.getMessage());
        }
    }
}
