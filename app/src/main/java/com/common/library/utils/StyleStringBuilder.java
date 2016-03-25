package com.common.library.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

/**
 * 
 * @author zhangfei
 *
 */
public class StyleStringBuilder {
	private SpannableStringBuilder builder = new SpannableStringBuilder();
	private int totalLength = 0;
	private Context context;
	
	public StyleStringBuilder(Context context){
		this.context = context;
	}
	
	public StyleStringBuilder appendPlainString(CharSequence content){
		builder.append(content);
		totalLength += content.length();
		return this;
	}
	
	public StyleStringBuilder appendForegroundColorString(CharSequence content, int color){
		builder.append(content);
		if(color > 0){
			builder.setSpan(new ForegroundColorSpan(context.getResources().getColor(color)), totalLength, 
					totalLength + content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		totalLength += content.length();
		return this;
	}
	
	public StyleStringBuilder appendFontSizeString(CharSequence content, int fontSizeId, boolean isBold){
	    builder.append(content);
	    builder.setSpan(new AbsoluteSizeSpan(context.getResources().getDimensionPixelSize(fontSizeId)),
	            totalLength, totalLength + content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    if (isBold) {
	        builder.setSpan(new StyleSpan(Typeface.BOLD),
	                totalLength, totalLength + content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    }
	    totalLength += content.length();
	    return this;
	}
	
	public StyleStringBuilder appendBackgroundColorString(CharSequence content, int color){
		builder.append(content);
		if(color > 0){
			builder.setSpan(new BackgroundColorSpan(context.getResources().getColor(color)), totalLength, 
					totalLength + content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		totalLength += content.length();
		return this;
	}
	
	public StyleStringBuilder appendClickableString(CharSequence linkString, final Runnable runable){
		builder.append(linkString);
		builder.setSpan(new ClickableSpan() {
			
			@Override
			public void onClick(View widget) {
				runable.run();
			}
		}, totalLength, totalLength + linkString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		totalLength += linkString.length();
		return this;
	}
	
	public StyleStringBuilder appendURLString(CharSequence linkString, String url){
		builder.append(linkString);
		builder.setSpan(new URLSpan(url), totalLength, totalLength + linkString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		totalLength += linkString.length();
		return this;
	}
	
	public StyleStringBuilder appendStrikethroughString(CharSequence content){
		builder.append(content);
		builder.setSpan(new StrikethroughSpan(), totalLength, totalLength + content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		totalLength += content.length();
		return this;
	}
	
	public StyleStringBuilder appendUnderlineString(CharSequence content){
		builder.append(content);
		builder.setSpan(new UnderlineSpan(), totalLength, totalLength + content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		totalLength += content.length();
		return this;
	}
	
	public Spanned build(){
		return builder;
	}
	
	public static CharSequence formatString(Context context, String content, String regular, int colorResId) {
		SpannableStringBuilder builder = new SpannableStringBuilder(content);
		Pattern p = Pattern.compile(regular);
		Matcher matcher = p.matcher(content);
		int start = 0;
		int end = 0;
		while (matcher.find()) {
			if (matcher.start() == end) {
				end = matcher.end();
			} else {
				if (start != end) {
					ForegroundColorSpan span = new ForegroundColorSpan(context.getResources().getColor(colorResId));
					builder.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				start = matcher.start();
				end = matcher.end();
			}
		}
		if (start != end) {
			ForegroundColorSpan span = new ForegroundColorSpan(context.getResources().getColor(colorResId));
			builder.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return builder;
	}
}
