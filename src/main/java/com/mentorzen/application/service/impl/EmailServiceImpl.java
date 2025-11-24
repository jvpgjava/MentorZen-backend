package com.mentorzen.application.service.impl;

import com.mentorzen.application.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Recuperação de Senha - Zen");

            String htmlContent = buildPasswordResetEmailHtml(resetUrl);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de recuperação de senha enviado para: {}", to);
        } catch (MessagingException e) {
            log.error("Erro ao enviar email de recuperação de senha para: {}", to, e);
            throw new RuntimeException("Erro ao enviar email de recuperação de senha", e);
        }
    }

    private String buildPasswordResetEmailHtml(String resetUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Recuperação de Senha - Zen</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f5f5f5;">
                <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td align="center" style="padding: 40px 20px;">
                            <table role="presentation" style="max-width: 600px; width: 100%%; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                <tr>
                                    <td style="padding: 40px 30px; text-align: center; background-color: #9ea04f; border-radius: 8px 8px 0 0;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: bold;">Zen</h1>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="margin: 0 0 20px 0; color: #162A41; font-size: 24px;">Recuperação de Senha</h2>
                                        <p style="margin: 0 0 20px 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                            Olá! Recebemos uma solicitação para redefinir a senha da sua conta no Zen.
                                        </p>
                                        <p style="margin: 0 0 30px 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                            Clique no botão abaixo para criar uma nova senha. Este link expira em 1 hora.
                                        </p>
                                        <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                            <tr>
                                                <td align="center" style="padding: 20px 0;">
                                                    <a href="%s" style="display: inline-block; padding: 14px 32px; background-color: #9ea04f; color: #ffffff; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 16px;">Redefinir Senha</a>
                                                </td>
                                            </tr>
                                        </table>
                                        <p style="margin: 30px 0 0 0; color: #999999; font-size: 14px; line-height: 1.6;">
                                            Se você não solicitou esta recuperação de senha, ignore este email. Sua senha permanecerá inalterada.
                                        </p>
                                        <p style="margin: 20px 0 0 0; color: #999999; font-size: 14px; line-height: 1.6;">
                                            Se o botão não funcionar, copie e cole este link no seu navegador:<br>
                                            <a href="%s" style="color: #9ea04f; word-break: break-all;">%s</a>
                                        </p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 20px 30px; background-color: #f5f5f5; border-radius: 0 0 8px 8px; text-align: center;">
                                        <p style="margin: 0; color: #999999; font-size: 12px;">
                                            Este é um email automático, por favor não responda.<br>
                                            © 2025 Zen - Desenvolvido pela equipe FloWrite
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, resetUrl, resetUrl, resetUrl);
    }
}

