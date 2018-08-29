package org.ivan2m;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class Main {
    public static void main(String[] args){
        try {
            udpateIndex();
        }catch(IOException e){
            e.printStackTrace();
        }

        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try{
            botsApi.registerBot(new FaqTfgBot());
        }catch(TelegramApiException e){
            e.printStackTrace();
        }
    }

    /**
     * Llamamos a la clase Indexer para crear o actualizar un índice en caso de que el usuario quiera,
     * porque hay mas archivos de FAQ por ejemplo
     * @throws IOException
     */
    private static void udpateIndex() throws IOException {
        String option = "";
        do {
            System.out.print("¿Deseas actualizar el índice con nuevos archivos FAQ?(S/n): ");
            System.out.println();

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            option = br.readLine();
            br.close();
        }while(!option.equals("S") && !option.equals("s") && !option.equals("N") && !option.equals("n"));

        if(option.equals("S") || option.equals("s")){
            Indexer indexer = new Indexer(true);
            indexer.createIndex();
            Map<String, Integer> numIndexed = indexer.getIndexInfo();
            indexer.close();

            System.out.println("Hay un total de " + numIndexed.get("totalQuestions") + " preguntas indexadas de " +
                    numIndexed.get("totalFAQs") + " FAQs (archivos), " + numIndexed.get("newFAQs") + " FAQs nuevos");
        }
    }
}
