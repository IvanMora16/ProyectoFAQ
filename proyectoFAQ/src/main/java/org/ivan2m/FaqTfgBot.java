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
        ArrayList<String> result = new ArrayList<>();
        DBConnect dbconnect = new DBConnect();


        //Comprobamos si hay mensaje y si en el mensaje hay texto
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage();

            if(!update.getMessage().isCommand()) {
                result = tester.searchQuestion(update.getMessage().getText());

                message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText(result.get(0));
                result.remove(0);

                dbconnect.saveNewAnswers(update.getMessage().getChatId(), result);
            }
            else{
                if(update.getMessage().getText().equals("/siguiente")){
                        message = new SendMessage()
                                .setChatId(update.getMessage().getChatId())
                                .setText(dbconnect.getNextAnswer(update.getMessage().getChatId()));
                } else if(update.getMessage().getText().equals("/start")){
                    dbconnect.saveChatId(update.getMessage().getChatId());

                    message = new SendMessage()
                            .setChatId(update.getMessage().getChatId())
                            .setText("Bienvenido");
                }
            }

            try {
                //Enviamos un mensaje para cada par pregunta/respuesta obtenido
//                for(int i = 0; i < messages.length; i++){
//                    execute(messages[i]);
                execute(message);
//                }
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
