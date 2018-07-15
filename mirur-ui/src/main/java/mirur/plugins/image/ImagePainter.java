/*
 * This file is part of Mirur.
 *
 * Mirur is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mirur is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mirur.  If not, see <http://www.gnu.org/licenses/>.
 */
package mirur.plugins.image;

import static com.jogamp.opengl.util.texture.TextureIO.newTexture;
import static com.metsci.glimpse.gl.util.GLUtils.disableBlending;
import static com.metsci.glimpse.gl.util.GLUtils.enableStandardBlending;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLProfile;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureData;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.shader.triangle.ColorTexture2DProgram;

public class ImagePainter extends GlimpsePainterBase {
    private static final Logger logger = getLogger(ImagePainter.class);

    final BufferedImage image;
    private Texture texture;
    private boolean initialized;

    private ColorTexture2DProgram prog;
    private GLEditableBuffer inXy;
    private GLEditableBuffer inS;

    public ImagePainter(BufferedImage image) {
        this.image = image;
        this.initialized = false;

        this.prog = new ColorTexture2DProgram();
        this.inXy = new GLEditableBuffer(GL.GL_STATIC_DRAW, 0);
        inXy.growQuad2f(0, 0, image.getWidth(), image.getHeight());
        this.inS = new GLEditableBuffer(GL.GL_STATIC_DRAW, 0);
        inS.growQuad2f(0, 1, 1, 0);
    }

    private void initIfNecessary(GL gl) {
        if (initialized) {
            return;
        }

        try {
            GLProfile profile = gl.getContext().getGLDrawable().getGLProfile();
            texture = newTexture(gl, new AWTTextureData(profile, 0, 0, false, image));
        } catch (Exception e) {
            logWarning(logger, "Failed to create image texture", e);
        }

        initialized = true;
    }

    @Override
    protected void doPaintTo(GlimpseContext context) {
        GL3 gl = getGL3(context);
        GlimpseBounds bounds = getBounds(context);

        initIfNecessary(gl);
        if (texture == null) {
            return;
        }

        texture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        texture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        texture.enable(gl);
        texture.bind(gl);

        enableStandardBlending(gl);
        prog.begin(context);
        try {
            prog.setPixelOrtho(context, bounds);
            prog.setTexture(context, 0);

            prog.draw(context, texture, inXy, inS);
        } finally {
            prog.end(context);
            disableBlending(gl);
        }
    }

    @Override
    protected void doDispose(GlimpseContext context) {
        prog.dispose(context);
        inXy.dispose(context.getGL());
        inS.dispose(context.getGL());
    }
}
