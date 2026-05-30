package com.banking.modules.transfer.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

import com.banking.modules.transfer.event.TransferCompletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j

public class NotificationService {
    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "transfer-completed", groupId = "notification-banking-group")
    public void listenTransferCompletedEvent(TransferCompletedEvent event) {
        log.info("Received transfer completed event: {}", event);
        sendEmail(event);
    }

    private void sendEmail(TransferCompletedEvent event) {
        if (event.getEmail() == null || event.getEmail().isBlank()) {
            log.info("Bỏ qua gửi email vì user chưa đăng ký nhận thông báo (email null). Tx: {}",
                    event.getTransactionId());
            return;
        }
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(event.getEmail());
            helper.setSubject("Thông báo giao dịch - TYB eBanking");

            String formattedTime = event.getTimestamp().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            String formattedDate = event.getTimestamp().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

            String htmlTemplate = """
                    <div style="font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; color: #333; line-height: 1.5;">
                        <!-- Header Banner -->
                        <div style="background-color: #14b789; color: white; padding: 25px 30px; text-align: left; border-radius: 8px 8px 0 0;">
                            <div style="font-size: 32px; font-weight: bold; display: inline-block;">
                                Tyb
                            </div>
                        </div>

                        <!-- Greeting -->
                        <div style="padding: 25px 30px 10px 30px;">
                            <p style="margin: 0 0 5px 0;">Cảm ơn Quý khách đã sử dụng dịch vụ TYB eBanking.</p>
                            <p style="margin: 0;">TYB xin thông báo giao dịch của Quý khách đã được thực hiện như sau:</p>
                        </div>

                        <!-- Details Table -->
                        <div style="padding: 10px 30px 30px 30px;">
                            <table style="width: 100%%; border-collapse: collapse; font-size: 14px;">
                                <tr>
                                    <td style="padding: 8px 15px 8px 0; font-weight: bold; width: 35%%; color: #444;">Ngày, giờ giao dịch</td>
                                    <td style="padding: 8px 0; color: #333;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px 8px 0; font-weight: bold; color: #444;">Loại giao dịch</td>
                                    <td style="padding: 8px 0; color: #333;">Chuyển tiền nhanh ngoài TYB</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px 8px 0; font-weight: bold; color: #444;">Số tham chiếu</td>
                                    <td style="padding: 8px 0; color: #333;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px 8px 0; font-weight: bold; color: #444;">Tài khoản trích nợ</td>
                                    <td style="padding: 8px 0; color: #333;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px 8px 0; font-weight: bold; color: #444;">Người thụ hưởng</td>
                                    <td style="padding: 8px 0; color: #333;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px 8px 0; font-weight: bold; color: #444;">Số tiền giao dịch</td>
                                    <td style="padding: 8px 0; color: #333;">(%s) %s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px 8px 0; font-weight: bold; color: #444;">Nội dung chuyển tiền</td>
                                    <td style="padding: 8px 0; color: #333;">Chuyen khoan</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px 8px 0; font-weight: bold; color: #444;">Cách thức lệnh</td>
                                    <td style="padding: 8px 0; color: #333;">Thanh toán ngay</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px 8px 0; font-weight: bold; color: #444;">Ngày nhập lệnh</td>
                                    <td style="padding: 8px 0; color: #333;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px 8px 0; font-weight: bold; color: #444;">Thời gian</td>
                                    <td style="padding: 8px 0; color: #333;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 15px 8px 0; font-weight: bold; color: #444;">Tình trạng</td>
                                    <td style="padding: 8px 0; color: #333;">Giao dịch thành công</td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    """
                    .formatted(
                            formattedTime,
                            event.getTransactionId(),
                            event.getFromAccountId(),
                            event.getToAccountId(),
                            event.getCurrency(),
                            String.format("%,d.00", event.getAmount().longValue()),
                            formattedDate,
                            formattedTime);

            helper.setText(htmlTemplate, true);
            javaMailSender.send(message);

            log.info("Đã gửi email HTML (Mẫu TYB) thành công tới {}", event.getEmail());
        } catch (Exception e) {
            log.error("Lỗi khi gửi email HTML: {}", e.getMessage(), e);
        }
    }
}
