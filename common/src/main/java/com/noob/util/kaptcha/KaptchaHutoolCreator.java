package com.noob.util.kaptcha;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.google.code.kaptcha.Producer;

import java.awt.image.BufferedImage;

// 图片验证码
public class KaptchaHutoolCreator implements Producer {
	
	private LineCaptcha lineCaptcha;
	
	public KaptchaHutoolCreator(int width, int heigth) {
		lineCaptcha = CaptchaUtil.createLineCaptcha(width, heigth);
	}

	@Override
	public BufferedImage createImage(String text) {
		return (BufferedImage) lineCaptcha.createImage(text);
	}

	@Override
	public synchronized String createText() {
		lineCaptcha.createCode();
		return lineCaptcha.getCode();
	}

}
