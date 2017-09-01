package com.merltech.tictactoe.graphics;

import java.io.InputStream;
import java.io.OutputStream;

public interface SVGService {
    boolean svg2png(InputStream input, OutputStream output);
}
