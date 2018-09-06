//MIT License
//
//        Copyright (c) 2016 Ruben Bermudez
//
//        Permission is hereby granted, free of charge, to any person obtaining a copy
//        of this software and associated documentation files (the "Software"), to deal
//        in the Software without restriction, including without limitation the rights
//        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//        copies of the Software, and to permit persons to whom the Software is
//        furnished to do so, subject to the following conditions:
//
//        The above copyright notice and this permission notice shall be included in all
//        copies or substantial portions of the Software.
//
//        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//        SOFTWARE.

package org.ivan2m;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;

public class FaqTfgBot extends TelegramLongPollingBot {
    private Searcher searcher;

    public FaqTfgBot(){
        searcher = new Searcher();
    }

    @Override
    public void onUpdateReceived(Update update) {
        ArrayList<String> result = new ArrayList<>();
        DBConnect dbconnect = new DBConnect();


        //Comprobamos si hay mensaje y si en el mensaje hay texto
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage();

            //Si no es un comando, se trata de una nueva pregunta
            if(!update.getMessage().isCommand()) {
                try {
                    result = searcher.searchQuery(update.getMessage().getText());
                }catch(IOException e){
                    e.printStackTrace();
                }

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
