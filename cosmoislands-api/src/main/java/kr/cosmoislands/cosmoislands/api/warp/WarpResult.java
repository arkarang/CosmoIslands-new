package kr.cosmoislands.cosmoislands.api.warp;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class WarpResult {

    private final boolean isSuccess;
    private final Throwable exception;
}
