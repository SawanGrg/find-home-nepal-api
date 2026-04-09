package com.beta.FindHome.factory.concrete;

import com.beta.FindHome.factory.factoryImpl.LocalImageFactoryImpl;
import com.beta.FindHome.factory.interfaces.ImageFactoryInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class ImageFactory {

    private final LocalImageFactoryImpl localImageFactoryImpl;

    @Autowired
    public ImageFactory(
            LocalImageFactoryImpl localImageFactory
    ){
        this.localImageFactoryImpl = localImageFactory;
    }

    public ImageFactoryInterface checkImageChannel(String channel) {
        if (channel == null || channel.isEmpty()) {
            return null;
        }

        switch (channel) {
            case "local":
                return localImageFactoryImpl;
            default:
                throw new IllegalArgumentException("unknown channel: " + channel);
        }
    }
}
