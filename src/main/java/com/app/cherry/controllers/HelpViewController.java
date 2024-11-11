package com.app.cherry.controllers;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class HelpViewController {
    @FXML
    private Text textDescription;

    public void init() {
        textDescription.setText("test");
//        textDescription.setText("Cherry is a lightweight application designed for basic text " +
//                "editing tasks. It allows users to create, open, edit, and save plain text files. " +
//                "Unlike feature-rich word processors, a simple text editor focuses on providing " +
//                "essential functionalities without the overhead of complex features. Typical features " +
//                "include basic text formatting, search and replace, and support for multiple file types. " +
//                "Its minimalistic interface ensures ease of use, making it ideal for coding, note-taking, " +
//                "and quick edits.");
//        if (RunApplication.resourceBundle.getLocale().getLanguage().equals("en")) {
//            textDescription.setText("Cherry is a lightweight application designed for basic text " +
//                    "editing tasks. It allows users to create, open, edit, and save plain text files. " +
//                    "Unlike feature-rich word processors, a simple text editor focuses on providing " +
//                    "essential functionalities without the overhead of complex features. Typical features " +
//                    "include basic text formatting, search and replace, and support for multiple file types. " +
//                    "Its minimalistic interface ensures ease of use, making it ideal for coding, note-taking, " +
//                    "and quick edits.");
//        } else {
//            textDescription.setText("Cherry — это легкое приложение, предназначенное для выполнения " +
//                    "базовых задач редактирования текста. Оно позволяет пользователям создавать, открывать, " +
//                    "редактировать и сохранять текстовые файлы. В отличие от полноценных текстовых процессоров, " +
//                    "простой текстовый редактор сосредоточен на предоставлении основных функций без лишней " +
//                    "сложности. Типичные функции включают базовое форматирование текста, поиск и замену, а также " +
//                    "поддержку различных типов файлов. Его минималистичный интерфейс обеспечивает простоту " +
//                    "использования, что делает его идеальным для кодирования, ведения заметок и быстрых правок.");
//        }
    }
}
