package com.muhlenberg.bot.listeners;

import java.io.IOException;

import com.symphony.bdk.core.service.message.exception.PresentationMLParserException;
import com.symphony.bdk.core.service.message.util.PresentationMLParser;
import com.symphony.bdk.gen.api.model.V4MessageSent;
import com.symphony.bdk.spring.events.RealTimeEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BergBotListener {
    private final BergBotController controller;

    public BergBotListener(BergBotController controller) {
        this.controller = controller;
    }

    @EventListener
    public void onMessageSent(RealTimeEvent<V4MessageSent> event) throws PresentationMLParserException {
        String msgText = PresentationMLParser.getTextContent(event.getSource().getMessage().getMessage());
        
        //Check if message is empty
        if(msgText==null) {
            return;
        }
        String[] msgParts = msgText.trim().split("\\s+");

        //Make sure user tagged the bot and started their message with a command
        if(!msgParts[0].equals("@BergBot") || msgParts[1].charAt(0) != '/'){
            return;
        }
        
        try {
            controller.handleIncoming(event, msgParts);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}