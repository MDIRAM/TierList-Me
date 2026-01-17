package tierlist;

import javax.swing.*;
import javax.swing.border.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class TierListGui extends JFrame {
    private TierListData tierListData;
    private TierManager manager;
    private JTextField itemNameField;
    private JComboBox<String> tierComboBox;
    private JPanel centerPanel;
    private JLabel titleLabel;
    private JLabel descriptionLabel;
    private String draggedItemName;
    private String draggedFromTier;
    private String selectedImagePath;
    private JPanel imagePalettePanel;
    private static final String PALETTE_SOURCE = "__PALETTE__";
    
    private static final String[] TIER_NAMES = {"S", "A", "B", "C", "D"};
    private static final Color[] TIER_COLORS = {
        new Color(255, 100, 100),
        new Color(255, 200, 100),
        new Color(255, 255, 100),
        new Color(100, 255, 100),
        new Color(100, 150, 255)
    };

    public TierListGui() {
        TierListData data = showInitialDialog();
        if (data == null) {
            System.exit(0);
        }
        
        this.tierListData = data;
        this.manager = data.getManager();
        setupUI();
    }

    private TierListData showInitialDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 240, 240));

        JLabel nameLabel = new JLabel("Nama Tier List:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        JTextField nameField = new JTextField(20);
        nameField.setText("My Tier List");
        panel.add(nameLabel);
        panel.add(nameField);

        JLabel descLabel = new JLabel("Deskripsi / Tujuan:");
        descLabel.setFont(new Font("Arial", Font.BOLD, 12));
        JTextArea descField = new JTextArea(3, 20);
        descField.setLineWrap(true);
        descField.setWrapStyleWord(true);
        descField.setText("Tier list untuk apa? (Contoh: Ranking Anime, Ranking Game, dll)");
        JScrollPane scrollPane = new JScrollPane(descField);
        panel.add(descLabel);
        panel.add(scrollPane);

        JLabel infoLabel = new JLabel("<html>Anda bisa menambah item dengan gambar nanti!</html>");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(100, 100, 100));
        panel.add(infoLabel);
        panel.add(new JLabel());

        int result = JOptionPane.showConfirmDialog(
            null,
            panel,
            "Buat Tier List Baru",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            return new TierListData(name.isEmpty() ? "My Tier List" : name, desc);
        }

        return null;
    }

    private void setupUI() {
        setTitle("Tier List Maker - " + tierListData.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 240, 240));

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.WEST);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        
        createTierPanels();
        
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(new Color(220, 220, 220));
        panel.setBorder(new TitledBorder("Informasi Tier List"));

        titleLabel = new JLabel(tierListData.getName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(40, 40, 40));
        panel.add(titleLabel, BorderLayout.NORTH);

        descriptionLabel = new JLabel(tierListData.getDescription());
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descriptionLabel.setForeground(new Color(80, 80, 80));
        panel.add(descriptionLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 10));
        panel.setBackground(new Color(220, 220, 220));
        panel.setBorder(new TitledBorder("Tambah Item"));
        panel.setPreferredSize(new Dimension(250, 300));

        panel.add(new JLabel("Nama Item:"));
        itemNameField = new JTextField();
        itemNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    addItem();
                }
            }
        });
        panel.add(itemNameField);

        panel.add(new JLabel("Pilih Tier:"));
        tierComboBox = new JComboBox<>(TIER_NAMES);
        tierComboBox.setSelectedIndex(0);
        panel.add(tierComboBox);

        JButton imageButton = new JButton("Pilih Gambar (Opsional)");
        imageButton.addActionListener(e -> selectImage());
        panel.add(imageButton);

        JButton addButton = new JButton("Tambah Item");
        addButton.setFont(new Font("Arial", Font.BOLD, 12));
        addButton.addActionListener(e -> addItem());
        panel.add(addButton);

        JButton loadFolderButton = new JButton("Muat Folder Gambar");
        loadFolderButton.addActionListener(e -> selectImageFolder());
        panel.add(loadFolderButton);

        return panel;
    }

    private void selectImageFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            loadImagePalette(dir);
            JOptionPane.showMessageDialog(this, "Folder gambar dimuat: " + dir.getName(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImagePath = fileChooser.getSelectedFile().getAbsolutePath();
            JOptionPane.showMessageDialog(this, "Gambar dipilih: " + new File(selectedImagePath).getName(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void createTierPanels() {
        centerPanel.removeAll();
        
        for (int i = 0; i < TIER_NAMES.length; i++) {
            JPanel tierPanel = new JPanel(new BorderLayout(5, 5));
            tierPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
            tierPanel.setBorder(new LineBorder(Color.BLACK, 2));
            tierPanel.setBackground(Color.WHITE);

            JLabel tierLabel = new JLabel(TIER_NAMES[i]);
            tierLabel.setFont(new Font("Arial", Font.BOLD, 32));
            tierLabel.setHorizontalAlignment(JLabel.CENTER);
            tierLabel.setPreferredSize(new Dimension(80, 110));
            tierLabel.setBackground(TIER_COLORS[i]);
            tierLabel.setOpaque(true);
            tierLabel.setForeground(Color.WHITE);
            tierPanel.add(tierLabel, BorderLayout.WEST);

            JPanel itemsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            itemsPanel.setBackground(Color.WHITE);
            
            Tier tier = manager.getTier(TIER_NAMES[i]);
            ArrayList<TierItem> items = tier.items;
            
            for (TierItem item : items) {
                JPanel itemButton = createItemPanel(item, TIER_NAMES[i]);
                itemsPanel.add(itemButton);
            }

            setupDropTarget(itemsPanel, TIER_NAMES[i]);

            tierPanel.add(itemsPanel, BorderLayout.CENTER);
            centerPanel.add(tierPanel);
        }
        // Pastikan panel palette tetap di bawah tier
        if (imagePalettePanel != null) {
            centerPanel.add(imagePalettePanel);
        }
        
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private JPanel createItemPanel(TierItem item, String currentTier) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(100, 110));
        panel.setBorder(new LineBorder(Color.GRAY, 1));
        panel.setBackground(new Color(200, 200, 200));

        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                BufferedImage img = ImageIO.read(new File(item.getImagePath()));
                Image scaledImg = img.getScaledInstance(100, 80, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
                imageLabel.setHorizontalAlignment(JLabel.CENTER);
                panel.add(imageLabel, BorderLayout.CENTER);
            } catch (Exception e) {
                JLabel errorLabel = new JLabel("Gambar Error");
                errorLabel.setHorizontalAlignment(JLabel.CENTER);
                panel.add(errorLabel, BorderLayout.CENTER);
            }
        }

        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 10));
        nameLabel.setHorizontalAlignment(JLabel.CENTER);
        nameLabel.setBackground(new Color(180, 180, 180));
        nameLabel.setOpaque(true);
        nameLabel.setPreferredSize(new Dimension(100, 25));
        panel.add(nameLabel, BorderLayout.SOUTH);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    showItemMenu(panel, item, currentTier);
                }
            }
        });

        new DragSource().createDefaultDragGestureRecognizer(
            panel,
            DnDConstants.ACTION_MOVE,
            dge -> {
                draggedItemName = item.getName();
                draggedFromTier = currentTier;
                dge.startDrag(DragSource.DefaultMoveDrop, new StringSelection(item.getName()));
            }
        );

        return panel;
    }

    private JPanel createPaletteItem(File imgFile) {
        TierItem fake = new TierItem(removeExtension(imgFile.getName()), imgFile.getAbsolutePath());
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(100, 110));
        panel.setBorder(new LineBorder(Color.GRAY, 1));
        panel.setBackground(new Color(230, 230, 230));

        try {
            BufferedImage img = ImageIO.read(imgFile);
            Image scaledImg = img.getScaledInstance(100, 80, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            panel.add(imageLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Gambar Error");
            errorLabel.setHorizontalAlignment(JLabel.CENTER);
            panel.add(errorLabel, BorderLayout.CENTER);
        }

        JLabel nameLabel = new JLabel(removeExtension(imgFile.getName()));
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        nameLabel.setHorizontalAlignment(JLabel.CENTER);
        nameLabel.setPreferredSize(new Dimension(100, 25));
        panel.add(nameLabel, BorderLayout.SOUTH);

        new DragSource().createDefaultDragGestureRecognizer(
            panel,
            DnDConstants.ACTION_COPY,
            dge -> {
                draggedItemName = removeExtension(imgFile.getName());
                draggedFromTier = PALETTE_SOURCE;
                dge.startDrag(DragSource.DefaultCopyDrop, new StringSelection(imgFile.getAbsolutePath()));
            }
        );

        return panel;
    }

    private String removeExtension(String name) {
        int i = name.lastIndexOf('.');
        if (i > 0) return name.substring(0, i);
        return name;
    }

    private void showItemMenu(JPanel panel, TierItem item, String currentTier) {
        JPopupMenu menu = new JPopupMenu();

        JMenu moveMenu = new JMenu("Pindah ke Tier");
        for (String tier : TIER_NAMES) {
            if (!tier.equals(currentTier)) {
                JMenuItem menuItem = new JMenuItem(tier);
                menuItem.addActionListener(e -> {
                    manager.moveItem(item.getName(), currentTier, tier);
                    refreshDisplay();
                });
                moveMenu.add(menuItem);
            }
        }
        menu.add(moveMenu);

        menu.addSeparator();
        JMenuItem deleteItem = new JMenuItem("Hapus");
        deleteItem.addActionListener(e -> {
            manager.getTier(currentTier).removeItem(item);
            refreshDisplay();
        });
        menu.add(deleteItem);

        menu.show(panel, 0, panel.getHeight());
    }

    private void setupDropTarget(JPanel panel, String targetTier) {
        new DropTarget(panel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    Transferable tr = dtde.getTransferable();
                    if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                        String data = (String) tr.getTransferData(DataFlavor.stringFlavor);
                        // Jika datang dari palette -> buat item baru dengan image path
                        if (PALETTE_SOURCE.equals(draggedFromTier)) {
                            String imagePath = data;
                            String name = draggedItemName != null ? draggedItemName : removeExtension(new File(imagePath).getName());
                            TierItem newItem = new TierItem(name, imagePath);
                            Tier t = manager.getTier(targetTier);
                            if (t != null) t.addItem(newItem);
                            refreshDisplay();
                        } else {
                            // pindah antar tier biasa
                            if (draggedFromTier != null && !draggedFromTier.equals(targetTier)) {
                                manager.moveItem(data, draggedFromTier, targetTier);
                                refreshDisplay();
                            }
                        }
                        dtde.dropComplete(true);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dtde.rejectDrop();
            }
        });
    }

    private void loadImagePalette(File dir) {
        if (dir == null || !dir.isDirectory()) return;
        if (imagePalettePanel == null) {
            imagePalettePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            imagePalettePanel.setBackground(new Color(245, 245, 245));
            imagePalettePanel.setBorder(new TitledBorder("Image Palette - Drag gambar ke tier"));
            imagePalettePanel.setPreferredSize(new Dimension(1000, 140));
        } else {
            imagePalettePanel.removeAll();
        }

        File[] files = dir.listFiles((f) -> {
            String n = f.getName().toLowerCase();
            return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif");
        });

        if (files != null) {
            for (File f : files) {
                imagePalettePanel.add(createPaletteItem(f));
            }
        }

        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(new Color(220, 220, 220));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshDisplay());
        panel.add(refreshButton);

        JButton clearButton = new JButton("Clear All");
        clearButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Hapus semua item?", 
                "Konfirmasi", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                manager = new TierManager();
                refreshDisplay();
            }
        });
        panel.add(clearButton);

        return panel;
    }

    private void addItem() {
        String name = itemNameField.getText().trim();
        String tier = (String) tierComboBox.getSelectedItem();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan nama item!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Tier t = manager.getTier(tier);
        if (t != null) {
            TierItem item = new TierItem(name, selectedImagePath);
            t.addItem(item);
            itemNameField.setText("");
            selectedImagePath = null;
            itemNameField.requestFocus();
            refreshDisplay();
            JOptionPane.showMessageDialog(this, "Item '" + name + "' ditambahkan ke tier " + tier, "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void refreshDisplay() {
        createTierPanels();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TierListGui());
    }
}
