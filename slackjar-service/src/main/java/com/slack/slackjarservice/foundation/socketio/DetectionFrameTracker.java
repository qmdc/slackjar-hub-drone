package com.slack.slackjarservice.foundation.socketio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DetectionFrameTracker {

    private final ConcurrentHashMap<String, FrameSession> sessions = new ConcurrentHashMap<>();

    public void createSession(String taskId) {
        sessions.put(taskId, new FrameSession());
    }

    public void addFrame(String taskId, String framePath) {
        FrameSession session = sessions.get(taskId);
        if (session != null) {
            session.addFrame(framePath);
        }
    }

    public void markComplete(String taskId) {
        FrameSession session = sessions.get(taskId);
        if (session != null) {
            session.markComplete();
        }
    }

    public void markError(String taskId) {
        FrameSession session = sessions.get(taskId);
        if (session != null) {
            session.markError();
        }
    }

    public FrameSession getSession(String taskId) {
        return sessions.get(taskId);
    }

    public void removeSession(String taskId) {
        sessions.remove(taskId);
    }

    public static class FrameSession {
        private final List<String> framePaths = new CopyOnWriteArrayList<>();
        private volatile boolean complete = false;
        private volatile boolean error = false;
        private volatile int lastNotifiedIndex = -1;
        private final CountDownLatch completionLatch = new CountDownLatch(1);

        public void addFrame(String path) {
            framePaths.add(path);
        }

        public void markComplete() {
            this.complete = true;
            this.completionLatch.countDown();
        }

        public void markError() {
            this.error = true;
            this.completionLatch.countDown();
        }

        public List<String> getFramePaths() {
            return framePaths;
        }

        public boolean isComplete() {
            return complete;
        }

        public boolean isError() {
            return error;
        }

        public int getLastNotifiedIndex() {
            return lastNotifiedIndex;
        }

        public void setLastNotifiedIndex(int index) {
            this.lastNotifiedIndex = index;
        }

        public boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
            return completionLatch.await(timeout, unit);
        }
    }
}
