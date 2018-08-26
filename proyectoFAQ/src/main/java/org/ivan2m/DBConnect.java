package org.ivan2m;

import java.sql.*;
import java.util.ArrayList;

public class DBConnect {
    private Connection con;
    private Statement st;

    public DBConnect() {
        String url = "jdbc:mysql://localhost:3306/tfg?serverTimezone=Europe/Madrid";
        String user = "root";
        String password = "";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
        } catch(Exception e) {
            System.out.println("Error: " + e);
        }
    }

    /**
     * Para guardar el identificador del chat
     * @param chatId
     */
    public void saveChatId(Long chatId){
        try {
            String query = "select id from chats where id='" + chatId + "';";
            ResultSet result = st.executeQuery(query);

            if(!result.next()) {
                query = "insert into chats (id) values (" + chatId + ");";
                st.executeUpdate(query);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Para guardar las respuestas a la última pregunta hecha por el usuario
     * @param chatId
     * @param questions
     */
    public void saveNewAnswers(Long chatId, ArrayList<String> questions){
        try {
            String query = "delete from preguntas where chat_id=" + chatId + ";";
            st.executeUpdate(query);

            for(int i = 0; i < questions.size(); i++){
                query = "insert into preguntas (pregunta, chat_id) values ('" + questions.get(i) + "'," + chatId + ");";
                st.executeUpdate(query);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Para devolver la siguiente respuesta a la última pregunta realizada por el usuario
     * @param chatId
     * @return
     */
    public String getNextAnswer(Long chatId){
        String answer = "";

        try {
            String query = "select * from preguntas where chat_id='" + chatId + "';";
            ResultSet result = st.executeQuery(query);

            if(result.next()){
                answer = result.getString("pregunta");
//                System.out.println(result.getObject("id"));
                query = "delete from preguntas where id=" + result.getLong("id") + ";";
                st.executeUpdate(query);
            }
            else{
                answer = "No quedan más respuestas para la última pregunta realizada";
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return answer;
    }
}

