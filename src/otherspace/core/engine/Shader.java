package otherspace.core.engine;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

/**
 * Stores a reference to a loaded shader program.
 */
public class Shader {
    // Store default shader
    public static Shader defaultShader = new Shader("resources/shaders/default/");

    // Fields
    private String path;
    private int shaderProgramID;
    private boolean inUse;

    public Shader(String shaderPath) {
        path = shaderPath;

        String vertSource;
        String fragSource;

        // Get the shader GLSL source
        try {
            vertSource = Files.readString(Paths.get(shaderPath + "shader.vert"));
            fragSource = Files.readString(Paths.get(shaderPath + "shader.frag"));
        } catch (IOException e) {
            throw new RuntimeException("ERROR: Failed to load shader at " + shaderPath);
        }

        // Compile and link the sourcecode
        compile(vertSource, fragSource);
    }

    /**
     * Compile the shader sourcecode.
     *
     * @param vertSource GLSL source for the vertex shader.
     * @param fragSource GLSL source for the fragment shader.
     */
    private void compile(String vertSource, String fragSource) {
        // Load and compile vertex shader
        int vertexID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexID, vertSource);
        glCompileShader(vertexID);

        // Check for compilation errors
        if (glGetShaderi(vertexID, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("ERROR: Failed to compile vertex shader at " + path + "\n" +
                    glGetShaderInfoLog(vertexID, glGetShaderi(vertexID, GL_INFO_LOG_LENGTH))
            );
        }

        // Load and compile fragment shader
        int fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentID, fragSource);
        glCompileShader(fragmentID);

        // Check for compilation errors
        if (glGetShaderi(fragmentID, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("ERROR: Failed to compile fragment shader at " + path + "\n" +
                    glGetShaderInfoLog(fragmentID, glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH))
            );
        }

        // Link shaders
        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        // Check for linking errors
        if (glGetProgrami(shaderProgramID, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("ERROR: Failed to link shader at " + path + "\n" +
                    glGetProgramInfoLog(shaderProgramID, glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH))
            );
        }
    }

    /**
     * Use this shader for drawing.
     */
    public void use() {
        if (!inUse) {
            // Bind shader
            glUseProgram(shaderProgramID);
            inUse = true;
        }
    }

    /**
     * Unload shader.
     */
    public void detach() {
        glUseProgram(0);
        inUse = false;
    }

    /**
     * Upload a 4x4 float matrix to the shader.
     *
     * @param varName Name of variable in shader.
     * @param value Value to set.
     */
    public void uploadMat4f(String varName, Matrix4f value) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        FloatBuffer buff = BufferUtils.createFloatBuffer(16);
        value.get(buff);
        glUniformMatrix4fv(varLocation, false, buff);
    }

    public void uploadIntArray(String varName, int[] array) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        glUniform1iv(varLocation, array);
    }
}
