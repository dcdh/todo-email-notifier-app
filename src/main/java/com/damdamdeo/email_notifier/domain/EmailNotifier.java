package com.damdamdeo.email_notifier.domain;

public interface EmailNotifier {

    void notify(final String subject, final String content);

}
