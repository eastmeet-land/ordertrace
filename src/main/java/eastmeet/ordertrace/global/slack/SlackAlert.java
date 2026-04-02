package eastmeet.ordertrace.global.slack;

public record SlackAlert(
    String emoji,
    String title,
    String body,
    String kibanaKeyword
) {}