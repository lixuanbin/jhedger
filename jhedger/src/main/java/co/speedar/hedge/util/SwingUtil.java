package co.speedar.hedge.util;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;

public class SwingUtil {
	public static void showDialog(String title, String content, int width, int height) {
		JFrame jFrame = new JFrame(title);
		JPanel jPanel = new JPanel();
		JTextPane jTextPane = new JTextPane();
		jTextPane.setText(content);
		jTextPane.setEditable(false);
		jPanel.add(jTextPane);
		jFrame.add(jPanel);
		jFrame.setSize(width, height);
		jFrame.setVisible(true);
		jFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}

	public static void main(String[] args) {
		showDialog("test", "haha", 500, 300);
	}
}
