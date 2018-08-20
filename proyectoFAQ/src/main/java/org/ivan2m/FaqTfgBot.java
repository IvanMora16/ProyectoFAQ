package org.ivan2m;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class FaqTfgBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        LuceneTester tester = new LuceneTester();

        //Comprobamos si hay mensaje y si en el mensaje hay texto
        if (update.hasMessage() && update.getMessage().hasText()) {
            String result = tester.searchQuestion(update.getMessage().getText());
            //Creamos un mensaje con texto setText al chat de setChatId
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText(result);
            try {
                //Enviamos el mensaje
                execute(message);
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
