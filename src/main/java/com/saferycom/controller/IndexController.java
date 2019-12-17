package com.saferycom.controller;

import com.saferycom.bean.AztResponse;
import com.saferycom.rbac.user.model.User;
import com.saferycom.rbac.user.service.IUserService;
import com.saferycom.util.enumData.AztResultEnum;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

@Controller
public class IndexController {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    private final IUserService userService;

    public IndexController(IUserService userService) {
        this.userService = userService;
    }

    /**
     * 主页
     *
     * @return index.html
     */
    @RequestMapping({"/"})
    public String index() {
        return "index";
    }

    /**
     * 修改密码
     *
     * @param id          用户ID
     * @param oldPassword 旧密码
     * @param password    新密码
     * @return {@link AztResponse}
     */
    @ResponseBody
    @PostMapping("/changePassword")
    public AztResponse changePassword(String id, String oldPassword, String password) {
        AztResponse response = new AztResponse();
        User user = userService.findUserById(id);
        if (oldPassword.equals(user.getPassword())) {
            user.setPassword(password);
            userService.updateUserById(user);
            response.setMsg("密码设置成功!");
            response.setResult(AztResultEnum.SUCCESS.getIndex());
        } else {
            response.setMsg("原密码错误!");
            response.setResult(AztResultEnum.FAIL.getIndex());
        }
        return response;
    }

    /**
     * 获取验证码，返回图片
     *
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     */
    @GetMapping("/verifyCodeImage")
    public void verifyCodeImage(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        //设置页面不缓存
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("Content-Type", "image/jpeg");

        // 在内存中创建图象
        int width = 60, height = 20;
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        // 获取图形上下文
        Graphics g = image.getGraphics();

        //生成随机类
        Random random = new Random();

        // 设定背景色
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);

        // 随机产生155条干扰线，使图象中的认证码不易被其它程序探测到
        //设定颜色
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 30; i++) {//20190114，将155改成10
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(10);
            int yl = random.nextInt(10);
            g.drawLine(x, y, x + xl, y + yl);
        }
        String chose = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        // 取随机产生的认证码(4位数字)

        char[] ran = {'0', '0', '0', '0'};
        char temp;
        Random rand = new Random();
        for (int i = 0; i < 4; i++) {
            temp = chose.charAt(rand.nextInt(chose.length()));
            ran[i] = temp;
            // 将认证码显示到图象中
            //设定颜色
            Color color = new Color(20 + random.nextInt(110), 20 + random
                    .nextInt(110), 20 + random.nextInt(110));
            g.setColor(color);
            //调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
            //设定字体
            g.setFont(new Font("Fixedsys", Font.PLAIN, 20));//20190114，将Times New Roman改成Fixedsys
            //将生成的随机数进行随机缩放并旋转制定角度 PS.建议不要对文字进行缩放与旋转,因为这样图片可能不正常显示
            Graphics2D g2d_word = (Graphics2D) g;
            AffineTransform trans = new AffineTransform();
            g2d_word.setTransform(trans);
            g.drawString(String.valueOf(temp), 15 * i, 14);
        }

        // 设置验证码
        session.setAttribute("verifyCode", String.valueOf(ran).toUpperCase());

        g.dispose();
        ServletOutputStream outStream = null;
        try {
            outStream = response.getOutputStream();
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(outStream);
            shear(g, image.getWidth(), image.getHeight());//扭曲图片

            // 添加噪点
            float yawpRate = 0.05f;// 噪声率
            int area = (int) (yawpRate * image.getWidth() * image.getHeight());
            for (int i = 0; i < area; i++) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);

                image.setRGB(x, y, random.nextInt(255));
            }
            encoder.encode(image);
        } catch (IOException e) {
            logger.error("", e);
        } finally {
            if (outStream != null) {
                try {
                    outStream.flush();
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Color getRandColor(int fc, int bc) {//给定范围获得随机颜色
        Random random = new Random();
        if (fc > 255)
            fc = 255;
        if (bc > 255)
            bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    // 扭曲方法
    private void shear(Graphics g, int w1, int h1) {
        shearX(g, w1, h1);
        shearY(g, w1, h1);
    }

    private Random random = new Random();

    private void shearX(Graphics g, int w1, int h1) {
        int period = random.nextInt(2);
        int frames = 1;
        int phase = random.nextInt(2);
        for (int i = 0; i < h1; i++) {
            double d = (double) (period >> 1)
                    * Math.sin((double) i / (double) period
                    + (6.2831853071795862D * (double) phase)
                    / (double) frames);
            g.copyArea(0, i, w1, 1, (int) d, 0);
            g.setColor(Color.white);
            g.drawLine((int) d, i, 0, i);
            g.drawLine((int) d + w1, i, w1, i);
        }
    }

    private void shearY(Graphics g, int w1, int h1) {
        int period = random.nextInt(40) + 10; // 50;
        int frames = 20;
        int phase = 7;
        for (int i = 0; i < w1; i++) {
            double d = (double) (period >> 1)
                    * Math.sin((double) i / (double) period
                    + (6.2831853071795862D * (double) phase)
                    / (double) frames);
            g.copyArea(i, 0, 1, h1, 0, (int) d);
            g.setColor(Color.white);
            g.drawLine(i, (int) d, i, 0);
            g.drawLine(i, (int) d + h1, i, h1);

        }
    }

}
