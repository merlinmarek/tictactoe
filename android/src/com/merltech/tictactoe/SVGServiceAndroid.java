package com.merltech.tictactoe;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.util.DisplayMetrics;
import android.util.Log;

import com.caverock.androidsvg.SVG;
import com.merltech.tictactoe.graphics.SVGService;

import java.io.InputStream;
import java.io.OutputStream;

public class SVGServiceAndroid implements SVGService {

    private final String Tag = "SVGService";
    private final float density;

    public SVGServiceAndroid(float density) {
        this.density = density;
    }

    @Override
    public boolean svg2png(InputStream input, OutputStream output) {
        try {
            SVG svg = SVG.getFromInputStream(input);
            svg.setDocumentHeight(svg.getDocumentHeight() * density / 200);
            svg.setDocumentWidth(svg.getDocumentWidth() * density / 200);
            Bitmap bitmap = Bitmap.createBitmap((int)Math.ceil(svg.getDocumentWidth()), (int)Math.ceil(svg.getDocumentHeight()), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap);
            svg.renderToCanvas(canvas);
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, output);
            return true;
        } catch(Exception e) {
            Log.e(Tag, "error parsing svg", e);
            return false;
        }
    }
}
