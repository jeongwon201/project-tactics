package com.spring.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.service.TacticsService;
import com.spring.vo.MemberVO;
import com.spring.vo.TacticsVO;

@Controller
@RequestMapping("/tactics/*")
public class TacticsController {

	@Inject
	TacticsService service;

	private static final Logger logger = LoggerFactory.getLogger(TacticsController.class);

	// 전술 만들기 페이지 이동, 셀렉트 박스에 전술 불러오기
	@RequestMapping(value = "tactics", method = RequestMethod.GET)
	public void tactics(TacticsVO tacticsVO, Model model, HttpSession session) throws Exception {
		logger.info("tactics");

		MemberVO memberVO = (MemberVO) session.getAttribute("member");
		String userId = memberVO.getUserId();

		model.addAttribute("tacticsList", service.tacticsList(userId));
	}

	// 전술 불러오기
	@RequestMapping(value = "tacticsLoad", method = RequestMethod.POST)
	@ResponseBody
	public String tacticsLoad(HttpServletRequest req, HttpSession session) throws Exception {
		logger.info("tacticsLoad");

		MemberVO memberVO = (MemberVO) session.getAttribute("member");
		String userId = memberVO.getUserId();
		String title = req.getParameter("title");

		TacticsVO tacticsVO = new TacticsVO();
		tacticsVO.setTitle(title);
		tacticsVO.setUserId(userId);

		tacticsVO = service.tacticsLoad(tacticsVO);
		String url = tacticsVO.getUrl();

		String data = "";
		try {
			File file = new File(url);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);
			data = bufReader.readLine();
			bufReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	// 전술 삭제
	@RequestMapping(value = "tacticsDelete", method = RequestMethod.POST)
	@ResponseBody
	public int tacticsDelete(HttpServletRequest req, HttpSession session) throws Exception {
		logger.info("tacticsDelete");

		int result;
		try {
			MemberVO memberVO = (MemberVO) session.getAttribute("member");
			String userId = memberVO.getUserId();
			String title = req.getParameter("title");

			TacticsVO tacticsVO = new TacticsVO();
			tacticsVO.setTitle(title);
			tacticsVO.setUserId(userId);

			try {
				tacticsVO = service.tacticsLoad(tacticsVO);
				String url = tacticsVO.getUrl();
				File file = new File(url);
				if (file.exists()) {
					file.delete();
					service.tacticsDelete(tacticsVO);
					result = 1;
				} else {
					result = 0;
				}
			} catch (FileNotFoundException e) {
				result = 0;
			} catch (IOException e) {
				System.out.println(e);
				result = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = 0;
		}
		return result;
	}

	//전술 저장
	@RequestMapping(value = "tacticsSave", method = RequestMethod.POST)
	@ResponseBody
	public int tacticsSave(HttpServletRequest req, HttpSession session) throws Exception {
		logger.info("tacticsSave");

		int result;
		try {
			MemberVO memberVO = (MemberVO) session.getAttribute("member");
			String userId = memberVO.getUserId();

			String textSaveName = req.getParameter("textSaveName");
			String scene = req.getParameter("sceneJson");
			String fileName = System.getProperty("user.dir") + "\\savefiles\\" + userId + "_" + textSaveName + ".txt";

			TacticsVO tacticsVO = new TacticsVO();
			tacticsVO.setTitle(textSaveName);
			tacticsVO.setUrl(fileName);
			tacticsVO.setUserId(userId);

			if (service.tacticsChk(tacticsVO) == 0) {
				saveTacticsFile(fileName, scene);
				service.tacticsInsert(tacticsVO);
			} else {
				deleteTacticsFile(fileName);
				saveTacticsFile(fileName, scene);
				service.tacticsUpdate(tacticsVO);
			}

			result = 1;
		} catch (Exception e) {
			e.printStackTrace();
			result = 0;
		}

		return result;
	}

	private void saveTacticsFile(String filename, String scene) {
		try {
			File file = new File(System.getProperty("user.dir") + "/savefiles");
			if(!file.exists()) {
				file.mkdir();
			}

			BufferedWriter fw = new BufferedWriter(new FileWriter(filename));
			fw.write(scene);
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteTacticsFile(String filename) {
		try {
			Files.delete(Paths.get(filename));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}