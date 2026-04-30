package com.slack.slackjarservice.drone.stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DetectionFrameTracker {

    private static final long SESSION_TIMEOUT_MS = 10 * 60 * 1000;

    private final ConcurrentHashMap<String, FrameSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "detection-session-cleanup");
        t.setDaemon(true);
        return t;
    });

    public DetectionFrameTracker() {
        cleanupScheduler.scheduleAtFixedRate(this::cleanupStaleSessions, 60, 60, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        cleanupScheduler.shutdownNow();
        sessions.forEach((taskId, session) -> session.destroy());
        sessions.clear();
    }

    private void cleanupStaleSessions() {
        long now = System.currentTimeMillis();
        sessions.forEach((taskId, session) -> {
            if (session.isComplete() || session.isError() || session.isDestroyed()) {
                if (now - session.getCreateTime() > SESSION_TIMEOUT_MS) {
                    sessions.remove(taskId);
                    log.info("[清理] 移除已结束的session: taskId={}", taskId);
                }
                return;
            }
            if (now - session.getCreateTime() > SESSION_TIMEOUT_MS) {
                log.warn("[清理] 超时未完成的session, 强制销毁: taskId={}, 帧数={}", taskId, session.getCurrentFrameIndex());
                session.destroy();
                sessions.remove(taskId);
            }
        });
    }

    public void createSession(String taskId) {
        sessions.put(taskId, new FrameSession());
    }

    public void createSession(String taskId, int totalFrames) {
        FrameSession session = new FrameSession();
        session.setTotalFrames(totalFrames);
        sessions.put(taskId, session);
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

    public boolean pauseSession(String taskId) {
        FrameSession session = sessions.get(taskId);
        if (session != null && !session.isComplete() && !session.isError()) {
            session.setPaused(true);
            return true;
        }
        return false;
    }

    public boolean resumeSession(String taskId) {
        FrameSession session = sessions.get(taskId);
        if (session != null && session.isPaused()) {
            session.setPaused(false);
            return true;
        }
        return false;
    }

    public FrameSession getSession(String taskId) {
        return sessions.get(taskId);
    }

    public void removeSession(String taskId) {
        sessions.remove(taskId);
    }

    public void destroySession(String taskId) {
        FrameSession session = sessions.get(taskId);
        if (session != null) {
            session.destroy();
        }
    }

    public static class FrameSession {
        private final List<String> framePaths = new CopyOnWriteArrayList<>();
        private final long createTime = System.currentTimeMillis();
        private volatile boolean complete = false;
        private volatile boolean error = false;
        private volatile boolean paused = false;
        private volatile boolean destroyed = false;
        private volatile int lastNotifiedIndex = -1;
        private volatile int totalFrames = 0;
        private volatile int currentFrameIndex = 0;
        private volatile int totalDetections = 0;
        private volatile long processTime = 0;
        private volatile Process process;
        private final CountDownLatch completionLatch = new CountDownLatch(1);

        public long getCreateTime() {
            return createTime;
        }

        public void addFrame(String path) {
            framePaths.add(path);
            currentFrameIndex = framePaths.size();
        }

        public void markComplete() {
            this.complete = true;
            this.completionLatch.countDown();
        }

        public void markError() {
            this.error = true;
            this.completionLatch.countDown();
        }

        public void destroy() {
            this.destroyed = true;
            if (this.process != null && this.process.isAlive()) {
                this.process.destroyForcibly();
                log.info("已强制销毁检测进程");
            }
            this.completionLatch.countDown();
        }

        public boolean isDestroyed() {
            return destroyed;
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

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }

        public int getLastNotifiedIndex() {
            return lastNotifiedIndex;
        }

        public void setLastNotifiedIndex(int index) {
            this.lastNotifiedIndex = index;
        }

        public int getTotalFrames() {
            return totalFrames;
        }

        public void setTotalFrames(int totalFrames) {
            this.totalFrames = totalFrames;
        }

        public int getCurrentFrameIndex() {
            return currentFrameIndex;
        }

        public int getTotalDetections() {
            return totalDetections;
        }

        public void addDetections(int count) {
            this.totalDetections += count;
        }

        public long getProcessTime() {
            return processTime;
        }

        public void setProcessTime(long processTime) {
            this.processTime = processTime;
        }

        public Process getProcess() {
            return process;
        }

        public void setProcess(Process process) {
            this.process = process;
        }

        public boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
            return completionLatch.await(timeout, unit);
        }
    }
}
