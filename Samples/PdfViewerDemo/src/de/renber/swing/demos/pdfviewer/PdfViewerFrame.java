package de.renber.swing.demos.pdfviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;

import de.renber.swing.controls.documentview.PageDisplay;
import de.renber.swing.controls.documentview.predefined.CompoundAdorner;
import de.renber.swing.controls.documentview.predefined.ContinuousFacingPagePreviewLayout;
import de.renber.swing.controls.documentview.predefined.ContinuousPagePreviewLayout;
import de.renber.swing.controls.documentview.predefined.FacingPagePreviewLayout;
import de.renber.swing.controls.documentview.predefined.PageNumberAdorner;
import de.renber.swing.controls.documentview.predefined.PageableSource;
import de.renber.swing.controls.documentview.predefined.ShadowPageAdorner;
import de.renber.swing.controls.documentview.predefined.SinglePagePreviewLayout;
import de.renber.swing.controls.documentview.types.PagePreviewEventListener;
import de.renber.swing.controls.documentview.types.PageSource;
import de.renber.swing.controls.documentview.types.PageVisibility;
import de.renber.swing.demos.pdfviewer.types.PdfPageSource;

import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import javax.swing.JSlider;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.Pageable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.DefaultComboBoxModel;

public class PdfViewerFrame extends JFrame {

	private JPanel contentPane;

	private JButton btnFirstPage;
	private JButton btnPreviousPage;
	private JButton btnNextPage;
	private JButton btnLastPage;

	private JSlider zoomSlider;
	private JLabel zoomValueLabel;
	private JButton btnFitPageToView;
	private JButton btnZoomOriginal;
	private JButton btnFitPageWidth;

	private JLabel lblPageNumber;

	private PageDisplay pageDisplay;

	private PDDocument pdfDoc;

	/**
	 * Update the buttons and status labels
	 */
	private void updateGUI() {
		int currentPage = pageDisplay.getCurrentPage();
		int viewElement = pageDisplay.getPreviewLayout().getViewElementIndex(currentPage);

		btnFirstPage.setEnabled(viewElement > 0);
		btnPreviousPage.setEnabled(viewElement > 0);
		btnNextPage.setEnabled(
				viewElement < pageDisplay.getPreviewLayout().getViewElementCount(pageDisplay.getPageCount()) - 1);
		btnLastPage.setEnabled(
				viewElement < pageDisplay.getPreviewLayout().getViewElementCount(pageDisplay.getPageCount()) - 1);

		zoomSlider.setEnabled(pdfDoc != null);
		btnFitPageToView.setEnabled(pdfDoc != null);
		btnZoomOriginal.setEnabled(pdfDoc != null);
		btnFitPageWidth.setEnabled(pdfDoc != null);

		PageVisibility[] visiblePages = pageDisplay.getVisiblePages();

		if (pdfDoc == null) {
			lblPageNumber.setText("No document loaded");
		} else {
			// get all pages whose visibility is larger than 50%
			List<PageVisibility> pg = new ArrayList<PageVisibility>();
			for (PageVisibility p : visiblePages)
				if (p.getVisiblePercentage() > 0.5)
					pg.add(p);

			if (currentPage == -1 || pg.size() == 0)
				lblPageNumber.setText(String.format("The document has %d Pages", pageDisplay.getPageCount()));
			else if (pg.size() == 1)
				lblPageNumber.setText(String.format("Page %d of %d", pg.get(0).getPageIndex() + 1, pageDisplay.getPageCount()));
			else if (pg.size() == 2)
				lblPageNumber.setText(String.format("Pages %d and %d of %d", pg.get(0).getPageIndex() + 1,
						pg.get(1).getPageIndex() + 1, pageDisplay.getPageCount()));
			else
				// more than two pages
				lblPageNumber.setText(String.format("Pages %d, %d and more of %d", pg.get(0).getPageIndex() + 1,
						pg.get(1).getPageIndex() + 1, pageDisplay.getPageCount()));
		}
	}

	/**
	 * Create the frame.
	 */
	public PdfViewerFrame() {
		setTitle("Pdf Viewer Demo");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 767, 491);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel viewerPanel = new JPanel();
		contentPane.add(viewerPanel, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel_1.add(panel, BorderLayout.NORTH);

		JButton btnOpenFile = new JButton("Open pdf");
		btnOpenFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// show file dialog to open a pdf file
				JFileChooser chooser = new JFileChooser();
				if (chooser.showOpenDialog(PdfViewerFrame.this) == JFileChooser.APPROVE_OPTION) {
					if (chooser.getSelectedFile().exists()) {
						try {
							PDDocument oldDoc = pdfDoc;
							pdfDoc = PDDocument.load(chooser.getSelectedFile());
							pageDisplay.setPageSource(new PdfPageSource(pdfDoc));

							if (oldDoc != null)
								oldDoc.close();

						} catch (IOException /*
												 * | IllegalArgumentException |
												 * PrinterException
												 */ e1) {
							JOptionPane.showMessageDialog(PdfViewerFrame.this, "Could not load pdf file.");
						}
					}
				}
			}
		});
		btnOpenFile.setIcon(new ImageIcon(
				PdfViewerFrame.class.getResource("/de/renber/swing/demos/pdfviewer/images/icons/fileOpen.png")));
		panel.add(btnOpenFile);

		JSeparator separator_3 = new JSeparator();
		separator_3.setPreferredSize(new Dimension(2, 20));
		separator_3.setOrientation(SwingConstants.VERTICAL);
		panel.add(separator_3);

		btnFirstPage = new JButton("First");
		btnFirstPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pageDisplay.gotoPage(0);
			}
		});
		btnFirstPage.setIcon(new ImageIcon(
				PdfViewerFrame.class.getResource("/de/renber/swing/demos/pdfviewer/images/icons/go_first.png")));
		panel.add(btnFirstPage);

		btnPreviousPage = new JButton("Previous");
		btnPreviousPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pageDisplay.gotoPreviousViewElement();
			}
		});
		btnPreviousPage.setIcon(new ImageIcon(
				PdfViewerFrame.class.getResource("/de/renber/swing/demos/pdfviewer/images/icons/go_previous.png")));
		panel.add(btnPreviousPage);

		btnNextPage = new JButton("Next");
		btnNextPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pageDisplay.gotoNextViewElement();
			}
		});
		btnNextPage.setIcon(new ImageIcon(
				PdfViewerFrame.class.getResource("/de/renber/swing/demos/pdfviewer/images/icons/go_next.png")));
		panel.add(btnNextPage);

		btnLastPage = new JButton("Last");
		btnLastPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pageDisplay.getPageCount() > 0)
					pageDisplay.gotoPage(pageDisplay.getPageCount() - 1);
			}
		});
		btnLastPage.setIcon(new ImageIcon(
				PdfViewerFrame.class.getResource("/de/renber/swing/demos/pdfviewer/images/icons/go_last.png")));
		panel.add(btnLastPage);

		JSeparator separator_4 = new JSeparator();
		separator_4.setPreferredSize(new Dimension(2, 20));
		separator_4.setOrientation(SwingConstants.VERTICAL);
		panel.add(separator_4);

		JLabel lblDisplayMode = new JLabel("Layout:");
		panel.add(lblDisplayMode);

		JComboBox layoutComboBox = new JComboBox();
		layoutComboBox.setModel(
				new DefaultComboBoxModel(new String[] { "Continuous", "Single Page", "Facing", "Continuous Facing" }));
		layoutComboBox.setPreferredSize(new Dimension(120, 20));
		layoutComboBox.setMinimumSize(new Dimension(120, 20));
		layoutComboBox.setBorder(new EmptyBorder(0, 0, 0, 0));
		layoutComboBox.setSelectedIndex(3);

		layoutComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					int idx = layoutComboBox.getSelectedIndex();

					int oldPage = pageDisplay.getCurrentPage();

					switch (idx) {
					case 0:
						pageDisplay.setPreviewLayout(new ContinuousPagePreviewLayout());
						break;
					case 1:
						pageDisplay.setPreviewLayout(new SinglePagePreviewLayout());
						break;
					case 2:
						pageDisplay.setPreviewLayout(new FacingPagePreviewLayout());
						break;
					case 3:
						pageDisplay.setPreviewLayout(new ContinuousFacingPagePreviewLayout());
						break;
					}

					// restore the current page
					pageDisplay.gotoPage(oldPage);
				}
			}
		});

		panel.add(layoutComboBox);

		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.SOUTH);

		JLabel lblZoom = new JLabel("Zoom:");
		panel_2.add(lblZoom);

		zoomSlider = new JSlider();
		zoomSlider.setMinimum(5);
		zoomSlider.setValue(100);
		zoomSlider.setMaximum(400);
		zoomSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				int z = zoomSlider.getValue();
				zoomValueLabel.setText(String.valueOf(z) + " %");

				// disable quality high scaling when zoom
				// value is subject to change
				pageDisplay.setAllowHiQualityScale(!zoomSlider.getValueIsAdjusting());
				pageDisplay.setZoomLevel(z / (float) 100);

				// disable auto fit
				btnFitPageToView.setSelected(false);
				btnFitPageWidth.setSelected(false);

			}
		});
		panel_2.add(zoomSlider);

		zoomValueLabel = new JLabel("100%");
		panel_2.add(zoomValueLabel);

		JSeparator separator_5 = new JSeparator();
		separator_5.setPreferredSize(new Dimension(2, 20));
		separator_5.setOrientation(SwingConstants.VERTICAL);
		panel_2.add(separator_5);

		btnZoomOriginal = new JButton("100%");
		btnZoomOriginal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pageDisplay.setZoomLevel(1);

				// disable auto fit
				btnFitPageToView.setSelected(false);
				btnFitPageWidth.setSelected(false);
			}
		});
		btnZoomOriginal.setIcon(new ImageIcon(
				PdfViewerFrame.class.getResource("/de/renber/swing/demos/pdfviewer/images/icons/zoomOriginal.png")));
		btnZoomOriginal.setActionCommand("ZoomOriginal");
		panel_2.add(btnZoomOriginal);

		btnFitPageToView = new JButton("Fit to view");
		btnFitPageToView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pageDisplay.fitPage();

				// disable auto fit
				btnFitPageToView.setSelected(true);
				btnFitPageWidth.setSelected(false);
			}
		});
		btnFitPageToView.setActionCommand("");
		btnFitPageToView.setIcon(new ImageIcon(
				PdfViewerFrame.class.getResource("/de/renber/swing/demos/pdfviewer/images/icons/zoomFitBest.png")));
		panel_2.add(btnFitPageToView);

		btnFitPageWidth = new JButton("Fit to width");
		btnFitPageWidth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pageDisplay.fitWidth();

				// disable auto fit
				btnFitPageToView.setSelected(false);
				btnFitPageWidth.setSelected(true);
			}
		});
		btnFitPageWidth.setIcon(new ImageIcon(
				PdfViewerFrame.class.getResource("/de/renber/swing/demos/pdfviewer/images/icons/zoomFitWidth.png")));
		btnFitPageWidth.setActionCommand("");
		panel_2.add(btnFitPageWidth);
		viewerPanel.setLayout(new BorderLayout(0, 0));

		// add the print preview panel
		pageDisplay = new PageDisplay();
		pageDisplay.setPageAdorner(new CompoundAdorner(new ShadowPageAdorner(5, Color.BLACK),
				new PageNumberAdorner(contentPane.getFont(), Color.BLACK)));
		pageDisplay.setPreviewLayout(new ContinuousFacingPagePreviewLayout());

		// event handling
		pageDisplay.addPagePreviewEventListeners(new PagePreviewEventListener() {
			@Override
			public void currentPageChanged() {
				updateGUI();
			}

			@Override
			public void zoomLevelChanged() {
				// update the zoom slider
				zoomSlider.setValue((int) (pageDisplay.getZoomlevel() * 100));
			}
		});

		viewerPanel.add(pageDisplay);

		JPanel panel_3 = new JPanel();
		contentPane.add(panel_3, BorderLayout.SOUTH);
		panel_3.setLayout(new BorderLayout(0, 0));

		lblPageNumber = new JLabel("Page %d of %d");
		lblPageNumber.setBorder(new EmptyBorder(2, 2, 2, 2));
		panel_3.add(lblPageNumber);

		// if the fit zoom modes are enabled
		// refit the page when the window changes its size
		this.getRootPane().addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				if (btnFitPageToView.isSelected()) {
					pageDisplay.fitPage();
				} else if (btnFitPageWidth.isSelected()) {
					pageDisplay.fitWidth();
				}
			}

		});

		// update the gui elements
		updateGUI();
	}

}
