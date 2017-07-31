package net.orekyuu.riho;

import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import com.intellij.refactoring.listeners.RefactoringEventListener;
import com.intellij.util.messages.MessageBusConnection;
import net.orekyuu.riho.character.CharacterBorder;
import net.orekyuu.riho.events.NotificationListener;
import net.orekyuu.riho.events.RefactoringListener;
import net.orekyuu.riho.topics.RihoReactionNotifier;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;

public class RihoPlugin implements ProjectComponent {

    private final Project project;

    public RihoPlugin(Project project) {
        this.project = project;
    }

    @Override
    public void initComponent() {
        EditorFactory.getInstance().addEditorFactoryListener(new EditorFactoryListener() {

            private MessageBusConnection connect;
            private CharacterBorder character = null;
            private HashMap<Editor, CharacterBorder> characterBorders = new HashMap<>();

            @Override
            public void editorCreated(@NotNull EditorFactoryEvent editorFactoryEvent) {
                Editor editor = editorFactoryEvent.getEditor();
                Project project = editor.getProject();
                if (project == null) {
                    return;
                }
                JComponent component = editor.getContentComponent();
                try {
                    CharacterBorder character = new CharacterBorder(component);
                    characterBorders.put(editor, character);
                    component.setBorder(character);
                    connect = project.getMessageBus().connect();
                    connect.subscribe(RihoReactionNotifier.REACTION_NOTIFIER, character);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void editorReleased(@NotNull EditorFactoryEvent editorFactoryEvent) {
                CharacterBorder characterBorder = characterBorders.get(editorFactoryEvent.getEditor());
                if (characterBorder != null) {
                    characterBorders.remove(editorFactoryEvent.getEditor());
                    characterBorder.dispose();
                }
            }
        }, () -> {
        });

        MessageBusConnection connect = project.getMessageBus().connect();
        connect.subscribe(Notifications.TOPIC, new NotificationListener(project));
        connect.subscribe(RefactoringEventListener.REFACTORING_EVENT_TOPIC, new RefactoringListener(project));
    }

    @Override
    public void disposeComponent() {
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "RihoPlugin";
    }

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {
    }
}
