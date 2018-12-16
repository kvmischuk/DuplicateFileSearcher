package com.epam.training.app;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Searcher {
    private Map<Long, List<Path>> map;
    private MyVisitor visitor;

    private Searcher() {
        map = new HashMap<>();
        visitor = this.new MyVisitor();
    }

    private void findDuplicates() throws IOException {
        for (Map.Entry<Long, List<Path>> entry : map.entrySet()) {
            List<Path> list = entry.getValue();
            if (list.size() < 2) continue;
            for (int i = 0; i < list.size() - 1; i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    Path file1 = list.get(i);
                    Path file2 = list.get(j);
                    if (isEqual(file1, file2)) {
                        printDuplicates(file1, file2);
                    }
                }
            }
        }
    }

    private boolean isEqual(Path file1, Path file2) {
        boolean isEqual = true;
        try (SeekableByteChannel channel1 = Files.newByteChannel(file1, StandardOpenOption.READ);
             SeekableByteChannel channel2 = Files.newByteChannel(file2, StandardOpenOption.READ)) {
            ByteBuffer b1 = ByteBuffer.allocate(100);
            ByteBuffer b2 = ByteBuffer.allocate(100);
            int bytes1 = channel1.read(b1);
            int bytes2 = channel2.read(b2);
            while (bytes1 != -1 || bytes2 != -1) {
                if (!Arrays.equals(b1.array(),b2.array())) {
                    isEqual = false;
                    break;
                }
                b1.clear();
                b2.clear();
                bytes1 = channel1.read(b1);
                bytes2 = channel2.read(b2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isEqual;
    }

    private void printDuplicates(Path file1, Path file2) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(file1, BasicFileAttributes.class);
        System.out.println("------------------");
        System.out.println("Duplicate №1 location: " + file1);
        System.out.println("Duplicate №2 location: " + file2);
        System.out.println("Files size: " + attributes.size() + "b");
    }

    public static void main(String[] args) {
        try {
            Searcher searcher = new Searcher();
            Path path = Paths.get(args[0]);
            Files.walkFileTree(path, searcher.visitor);
            searcher.findDuplicates();
        } catch (Exception e) {
            System.out.println("Something went wrong, check inputs and restart app");
        }
    }

    private class MyVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (map.get(attrs.size()) != null) {
                map.get(attrs.size()).add(file);
            } else {
                List<Path> list = new ArrayList<>();
                list.add(file);
                map.put(attrs.size(), list);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
