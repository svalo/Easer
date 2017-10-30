package ryey.easer.core.data.storage.backend.json.event;

import android.content.Context;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ryey.easer.commons.IllegalStorageDataException;
import ryey.easer.core.data.EventStructure;
import ryey.easer.core.data.storage.backend.EventDataStorageBackendInterface;
import ryey.easer.core.data.storage.backend.FileDataStorageBackendHelper;
import ryey.easer.core.data.storage.backend.IOUtils;
import ryey.easer.core.data.storage.backend.json.NC;

public class JsonEventDataStorageBackend implements EventDataStorageBackendInterface {

    private static JsonEventDataStorageBackend instance = null;
    private static Context s_context = null;
    private static File dir;

    public static JsonEventDataStorageBackend getInstance(Context context) {
        if (instance == null) {
            if (context != null)
                s_context = context;
            dir = IOUtils.mustGetSubDir(s_context.getFilesDir(), "event");
            instance = new JsonEventDataStorageBackend();
        }
        return instance;
    }

    private JsonEventDataStorageBackend() {
    }

    @Override
    public boolean has(String name) {
        return IOUtils.fileExists(dir, name + NC.SUFFIX);
    }

    @Override
    public List<String> list() {
        ArrayList<String> list = new ArrayList<>();
        for (EventStructure event : all()) {
            list.add(event.getName());
        }
        return list;
    }

    @Override
    public EventStructure get(String name) throws FileNotFoundException, IllegalStorageDataException {
        File file = new File(dir, name + NC.SUFFIX);
        return get(file);
    }

    private EventStructure get(File file) throws FileNotFoundException, IllegalStorageDataException {
        EventParser parser = new EventParser();
        return FileDataStorageBackendHelper.get(parser, file);
    }

    @Override
    public void write(EventStructure event) throws IOException {
        File file = new File(dir, event.getName() + NC.SUFFIX);
        EventSerializer serializer = new EventSerializer();
        FileDataStorageBackendHelper.write(serializer, file, event);
    }

    @Override
    public void delete(String name) {
        File file = new File(dir, name + NC.SUFFIX);
        if (!file.delete())
            throw new IllegalStateException("Unable to delete file " + file);
    }

    @Override
    public void update(EventStructure event) throws IOException {
        delete(event.getName());
        write(event);
    }

    @Override
    public List<EventStructure> all() {
        List<EventStructure> list = new ArrayList<>();
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile()) {
                    if (pathname.getName().endsWith(NC.SUFFIX)) {
                        return true;
                    }
                }
                return false;
            }
        });
        for (File file : files) {
            EventStructure event = null;
            try {
                event = get(file);
                list.add(event);
            } catch (IllegalStorageDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e.getCause());
            }
        }
        return list;
    }
}
