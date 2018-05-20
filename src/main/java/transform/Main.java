package transform;

import java.io.IOException;

import javax.swing.SwingUtilities;

import org.dom4j.DocumentException;
public class Main {

	public static void main(String[] args) throws DocumentException, IOException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Gui();
			}
		});

	}

}