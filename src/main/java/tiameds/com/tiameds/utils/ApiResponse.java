package tiameds.com.tiameds.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse <T>{
    private String status;
    private String message;
    private T data;

}
