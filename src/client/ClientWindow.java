package client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.JEditorPane;

public class ClientWindow {

	private static JFrame frmChat;
	private JTextField messageField;
	private static JTextArea textArea = new JTextArea();
	private static JEditorPane editorPane = new JEditorPane();

	private MessageClient client;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					ClientWindow window = new ClientWindow();
					window.frmChat.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientWindow() {
		initialize();
		String name = JOptionPane.showInputDialog("Enter your nick:");
		client = new MessageClient("localhost", 3000, name);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmChat = new JFrame();
		frmChat.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				client.disconnect();
			}
		});
		frmChat.setTitle("Chat");
		frmChat.setResizable(false);
		frmChat.setBounds(100, 100, 570, 405);
		frmChat.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		textArea.setEditable(false);
		


		
	
		editorPane.setEditable(false);
		
		JScrollPane scrollPane = new JScrollPane(editorPane);
		frmChat.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		frmChat.getContentPane().add(panel, BorderLayout.SOUTH);

		messageField = new JTextField();
		panel.add(messageField);
		messageField.setColumns(25);

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(e -> {
			if (!messageField.getText().equals("")) {
				client.onClick(messageField.getText());
				messageField.setText("");
			}
		});

		panel.add(btnSend);
		
		JButton btnSendfile = new JButton("SendFile");
		btnSendfile.addActionListener(e -> {
			File file = OpenDialog();
			if(file != null) {
				//client.sendFile(file);
			}else {
				ClientWindow.print("!!Ошибка: Не удалось прикрепить файл");
			}
		});
		panel.add(btnSendfile);
		
		JButton btnUploadFile = new JButton("Upload File");
		btnUploadFile.addActionListener(e -> {
			File file = OpenDialog();
			if(file != null) {
				//client.uploadFile(file);
			}else {
				ClientWindow.print("!!Ошибка: Не удалось прикрепить файл");
			}
		});
		panel.add(btnUploadFile);

		editorPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				// TODO Auto-generated method stub
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					System.out.println(e.getDescription());
				}

			}
		});
		
		frmChat.setLocationRelativeTo(null);

	}
	
	private File OpenDialog() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("."));
		int result = fileChooser.showOpenDialog(frmChat);
		if (result == JFileChooser.APPROVE_OPTION) {
		    File selectedFile = fileChooser.getSelectedFile();
		    System.out.println("Selected file: " + selectedFile.getName());
		    return selectedFile;
		}else {
			return null;
		}
	}

	public static void changeFormTitle(String title) {
		frmChat.setTitle(title);
	}

	public static void print(String message) {
		//textArea.setText(textArea.getText() + message + "\n");
		editorPane.setText(editorPane.getText() + message + "\n");
	}

	public static void clearConsole() {
		//textArea.setText("");
		editorPane.setText("");
	}

}
