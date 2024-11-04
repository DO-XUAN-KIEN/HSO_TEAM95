package Game.ai;

import Game.core.Manager;
import Game.core.ServerManager;
import Game.core.Util;
import Game.io.Session;
import Game.map.Map;
import Game.core.SQL;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotManager extends TelegramLongPollingBot {
    private final String botToken = "6597652769:AAFF6keBWiTFUnm6oXJIM1m5hQQw2UmEm7I";
    private final String chatId = "6526615822"; // Thay bằng chat ID của bạn

    @Override
    public String getBotUsername() {
        return "backuphso_bot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void sendNotification(String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        try {
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendNotificationOther(String message, String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        try {
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void processCommand(String command) throws IOException {
        // Xử lý lệnh được gửi đến bot
        if (command.startsWith("ban")) {
            String[] strs = command.split(" ");
            if (strs.length == 2) {
                Session.banAcc(strs[1]);
            } else {
                sendNotification("Lỗi cú pháp");
            }
        } else if (command.equals("/memory")) {
            int num = 0;
            for (Map[] maps : Map.entrys) {
                for (Map map_ : maps) {
                    num += map_.players.size();
                }
            }
            sendNotification("Bộ nhớ máy chủ hiện tại : " + ServerManager.getMemory() + "%\n" +
                    "Số người online " + num);
        } else if (command.equals("/accept")) {
            Manager.gI().isServerAdmin = !Manager.gI().isServerAdmin;
            sendNotification("Đã cho phép người chơi truy cập");
        } else if (command.startsWith("nap")) {
            String[] strs = command.split(" ");
            if (strs.length == 4) {
                topUp(strs[1], Integer.parseInt(strs[2]), Integer.parseInt(strs[3]));
            } else {
                sendNotification("Lỗi cú pháp");
            }
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        long userId = update.getMessage().getFrom().getId();
        if (userId != Long.parseLong(this.chatId)) {
            sendNotificationOther("Bạn không phải admin của server", String.valueOf(userId));
            return;
        }
        String command = update.getMessage().getText();
        try {
            processCommand(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void topUp(String name, int amount, int tigia) {
        try (Connection conn = SQL.gI().getConnection(); Statement statement = conn.createStatement()) {
            if (statement.executeUpdate(
                    "UPDATE `account` SET `coin` = `coin` +" + (amount * tigia) + ", `tiennap` = `tiennap` + " + amount + " WHERE `user` = '" + name + "';") > 0) {
                conn.commit();
                sendNotification("Đã nạp cho tài khoản " + name + " số coin " + Util.number_format(((long) amount * tigia)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendNotification("Lỗi nạp tài khoản " + name + " " + e.getMessage());
        }
    }

    private void sendFile() {
        String filePath = "backup.sql";
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));

            File tempFile = File.createTempFile("backup_mobile", ".sql");
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(fileBytes);
            fos.close();
            InputFile inputFile = new InputFile().setMedia(tempFile);

            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(inputFile);
            execute(sendDocument);
        } catch (TelegramApiException e) {

        } catch (IOException ex) {
            Logger.getLogger(BotManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void runBot() {
        try {
            Runtime.getRuntime().exec("cmd /c start backuptool.bat");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendFile();
            }
        }, 0, 1800000); // 30 phut 1 lần
    }
}
