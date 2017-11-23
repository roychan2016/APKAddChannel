package com.tencent.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Properties;
import java.util.zip.ZipException;

public final class ApkExternalInfoTool
{
    private static class ApkExternalInfo
    {
        Properties p = new Properties();
        byte[] otherData;

        void decode(byte[] data) throws IOException
        {
            if (null == data)
            {
                return;
            }
            ByteBuffer bb = ByteBuffer.wrap(data);
            int headLength = protoHead.getBytes().length;
            byte[] d = new byte[headLength];
            bb.get(d);
            // 未知协议抛出异常
            if (!protoHead.equals(new ZipShort(d)))
            {
                throw new ProtocolException("unknow protocl [" + Arrays.toString(d) + "]");
            }
            if (data.length - headLength <= 2)
            {
                return;
            }
            // 读取树后面数据的长度
            d = new byte[2];
            bb.get(d);
            int len = new ZipShort(d).getValue();

            if (data.length - headLength - 2 < len)
            {
                return;
            }
            // 读取协议数据
            d = new byte[len];
            bb.get(d);
            p.load(new ByteArrayInputStream(d));
            // 读取剩余数据
            int leftLen = data.length - headLength - len - 2;
            if (leftLen > 0)
            {
                otherData = new byte[leftLen];
                bb.get(otherData);
            }
        }

        byte[] encode() throws IOException
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // 写入协议头
            out.write(protoHead.getBytes());
            // 写入协议体
            String s = "";
            for (Object k : p.keySet())
            {
                s += k + "=" + p.getProperty((String) k) + "\r\n";
            }
            byte[] bytes = s.getBytes();
            // 写入长度信息
            out.write(new ZipShort(bytes.length).getBytes());
            out.write(bytes);
            if (null != otherData)
            {
                out.write(otherData);
            }
            return out.toByteArray();
        }

        @Override
        public String toString()
        {
            return "ApkExternalInfo [p=" + p + ", otherData=" + Arrays.toString(otherData) + "]";
        }
    }

    private static final int MIN_EOCD_SIZE =
    /* end of central dir signature */4 +
    /* number of this disk */2 +
    /* number of the disk with the */+
    /* start of the central directory */2 +
    /* total number of entries in */+
    /* the central dir on this disk */2 +
    /* total number of entries in */+
    /* the central dir */2 +
    /* size of the central directory */4 +
    /* offset of start of central */+
    /* directory with respect to */+
    /* the starting disk number */4 +
    /* zipfile comment length */2;
    private static final int CFD_LOCATOR_OFFSET =
    /* end of central dir signature */4 +
    /* number of this disk */2 +
    /* number of the disk with the */+
    /* start of the central directory */2 +
    /* total number of entries in */+
    /* the central dir on this disk */2 +
    /* total number of entries in */+
    /* the central dir */2 +
    /* size of the central directory */4;

    private static final ZipLong EOCD_SIG = new ZipLong(101010256L);
    public static final String CHANNELID = "channelNo";
    private static final ZipShort protoHead = new ZipShort(38651);

    /**
     * 从指定的文件中读取指定的key的值
     * 
     * @param apkFile
     * @param key
     * @return
     * @throws IOException
     */
    public static String read(File apkFile, String key) throws IOException
    {
        RandomAccessFile archive = null;
        try
        {
            archive = new RandomAccessFile(apkFile, "r");
            byte[] readComment = readComment(archive);
            if (null == readComment)
            {
                return null;
            }
            ApkExternalInfo apkExternalInfo = new ApkExternalInfo();
            apkExternalInfo.decode(readComment);
            return apkExternalInfo.p.getProperty(key);
        }
        finally
        {
            if (null != archive)
            {
                archive.close();
            }
        }

    }

    /**
     * 从给定的apk文件中读取渠道号信息
     * 
     * @param apkFile 需要读取渠道号的文件
     * @return 如果存在渠道号，则返回渠道号信息，否则返回空
     * @throws IOException
     */
    public static String readChannelId(File apkFile) throws IOException
    {
        return read(apkFile, CHANNELID);

    }

    /**
     * 读取文件的注释信息
     * 
     * @param archive
     * @return 注释信息的二进制内容
     * @throws IOException
     */
    private static byte[] readComment(RandomAccessFile archive) throws IOException
    {
        // 跳转到一个接近0X06054B50L标识的位置
        long off = archive.length() - MIN_EOCD_SIZE;
        archive.seek(off);
        byte[] sig = EOCD_SIG.getBytes();
        int curr = archive.read();

        boolean found = false;
        // 查找0X06054B50L标记的开头位置
        while (curr != -1)
        {
            if (curr == sig[0])
            {
                curr = archive.read();
                if (curr == sig[1])
                {
                    curr = archive.read();
                    if (curr == sig[2])
                    {
                        curr = archive.read();
                        if (curr == sig[3])
                        {
                            found = true;
                            break;
                        }
                    }
                }
            }
            archive.seek(--off);
            curr = archive.read();
        }
        if (!found)
        {
            throw new ZipException("archive is not a ZIP archive");
        }

        // 跳转到注释的长度字段位置
        archive.seek(off + CFD_LOCATOR_OFFSET + 4);
        // 读取注释长度
        byte[] data = new byte[2];
        archive.readFully(data);
        // 根据注释长度读取注释内容
        int length = new ZipShort(data).getValue();
        if (length == 0)
        {
            return null;
        }
        data = new byte[length];
        archive.read(data);
        return data;
    }

    /**
     * 将指定的键值对写入到给定的文件注释中
     * 
     * @param file
     * @param key
     * @param value
     * @throws IOException
     */
    public static void updateExternalInfo(File file, String key, String value) throws IOException
    {
        RandomAccessFile archive = null;
        try
        {
            archive = new RandomAccessFile(file, "rw");
            byte[] comment = readComment(archive);
            ApkExternalInfo apkExternalInfo = new ApkExternalInfo();
            apkExternalInfo.decode(comment);
            apkExternalInfo.p.setProperty(key, value);

            // 跳转到注释的起始位置
            archive.seek(archive.length() - (null == comment ? 0 : comment.length) - 2);
            // 将更新后的内容写入到文件中
            comment = apkExternalInfo.encode();
            archive.write(new ZipShort(comment.length).getBytes());
            archive.write(comment);

        }
        finally
        {
            if (null != archive)
            {
                archive.close();
            }
        }

    }
}
