package org.ivan2m;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;

public class FaqTfgBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        LuceneTester tester = new LuceneTester();

        //Comprobamos si hay mensaje y si en el mensaje hay texto
        if (update.hasMessage() && update.getMessage().hasText()) {
            ArrayList<String> result = tester.searchQuestion(update.getMessage().getText());
            SendMessage[] messages = new SendMessage[result.size()];

            //Creamos un mensaje con cada par pregunta respuesta con texto setText al chat de setChatId
            for(int i = 0; i < messages.length; i++){
                messages[i] = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText(result.get(i));
            }

            try {
                //Enviamos un mensaje para cada par pregunta/respuesta obtenido
                for(int i = 0; i < messages.length; i++){
                    execute(messages[i]);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "FAQtfg_bot";
    }

    @Override
    public String getBotToken() {
        return "623957008:AAETpiN9rjRGoQ_9ukyOZNHIH5I6g_1TZHs";
    }
}
