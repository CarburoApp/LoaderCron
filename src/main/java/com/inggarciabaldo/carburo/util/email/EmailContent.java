package com.inggarciabaldo.carburo.util.email;

import java.io.File;
import java.util.List;

/**
 * Record que representa un correo completamente construido
 * pero aún no enviado.
 */
public record EmailContent(String subject, String htmlBody, List<File> attachments) {

}
