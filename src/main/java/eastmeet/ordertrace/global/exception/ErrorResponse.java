package eastmeet.ordertrace.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

@Schema(description = "에러 응답")
public record ErrorResponse(
    @Schema(description = "HTTP 상태 코드", example = "404")
    int status,

    @Schema(description = "에러 유형", example = "Not Found")
    String error,

    @Schema(description = "에러 메시지", example = "주문을 찾을 수 없습니다. id: 1")
    String message,

    @Schema(description = "에러 발생 시각")
    LocalDateTime timestamp
) {

    public static ErrorResponse of(HttpStatus httpStatus, String message) {
        return new ErrorResponse(
            httpStatus.value(),
            httpStatus.getReasonPhrase(),
            message,
            LocalDateTime.now()
        );
    }
}