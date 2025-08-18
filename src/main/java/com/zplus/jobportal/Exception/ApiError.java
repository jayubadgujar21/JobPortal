package com.zplus.jobportal.Exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiError extends  RuntimeException{

    private int status;
    private String message;
    private String path;


    public ApiError(int status, String message) {
        super(message);
        this.status = status;
        this.message=message;
    }

    public int getStatus() {
        return status;
    }
}
