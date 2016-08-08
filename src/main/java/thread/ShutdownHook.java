package thread;

import util.settings.Settings;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by Nick on 8/8/2016.
 * <p>
 * Used for doing things on shutdown.
 */
public class ShutdownHook extends Thread
{
    @Override
    public void run()
    {
        deleteTempOnExit();
    }

    // Deletes the temp directory and contents
    public static void deleteTempOnExit()
    {
        if (!Files.exists(Settings.tmpDir.toPath()))
        {
            return;
        }
        try
        {
            Files.walkFileTree(Settings.tmpDir.toPath(), new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException
                {
                    Files.deleteIfExists(dir);
                    return super.postVisitDirectory(dir, exc);
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException
                {
                    Files.deleteIfExists(file);
                    return super.visitFile(file, attrs);
                }
            });
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
