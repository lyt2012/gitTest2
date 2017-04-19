package com.netease.course.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import com.netease.course.dao.ContentDao;
import com.netease.course.dao.PersonDao;
import com.netease.course.dao.TrxDao;
import com.netease.course.utils.BuyList;
import com.netease.course.utils.Content;
import com.netease.course.utils.Person;
import com.netease.course.utils.Product;
import com.netease.course.utils.Trx;

@Controller
public class HomeController {
	// 返回index页面
	@RequestMapping(value = "/")
	public String Index(HttpServletRequest request, ModelMap map) {
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-mybatis.xml");
		ContentDao dao = context.getBean("contentDao", ContentDao.class);
		List<Content> contents = dao.getContentList();
		List<Product> productList = new ArrayList<Product>();
		for (Content content : contents) {
			Product product = new Product();
			product.setId(content.getId());
			product.setImage(content.getIcon());
			product.setPrice(content.getPrice());
			product.setTitle(content.getTitle());
			productList.add(product);
		}

		HttpSession session = request.getSession();
		if (session.getAttribute("userName") != null) {
			Person user = new Person();
			user.setUsername((String) session.getAttribute("userName"));
			user.setUsertype((Integer) session.getAttribute("userType"));
			map.addAttribute("user", user);

			TrxDao dao2 = context.getBean("trxDao", TrxDao.class);

			List<Trx> trxs = dao2.geTrxList();
			for(Trx trx : trxs){
				for(Product p : productList){
					if(p.getId() == trx.getContentId()){
						p.setIsBuy(true);
						p.setIsSell(true);
					}
				}
			}		
		}
		map.addAttribute("productList", productList);
		((ConfigurableApplicationContext) context).close();
		return "index";
	}

	// 返回login页面
	@RequestMapping(value = "/login")
	public String Login() {
		return "login";
	}

	// 检验登录
	@RequestMapping(value = "/api/login", method = POST)
	public ModelMap LoginCheck(@RequestParam("userName") String userName, @RequestParam("password") String password,
			HttpServletRequest request) {
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-mybatis.xml");
		PersonDao persondao = context.getBean("personDao", PersonDao.class);
		List<Person> persons = persondao.getPersonList();
		ModelMap map = new ModelMap();
		for (Person person : persons) {
			if ((userName.equals(person.getUsername()) && (password.equals(person.getPassword())))) {

				map.addAttribute("code", 200);
				map.addAttribute("message", "OK");
				map.addAttribute("result", true);

				HttpSession session = request.getSession();
				session.setAttribute("userName", person.getUsername());
				session.setAttribute("userType", person.getUsertype());
				((ConfigurableApplicationContext) context).close();
				return map;
			}
		}
		map.addAttribute("code", 202);
		map.addAttribute("message", "账号或密码错误");
		map.addAttribute("result", true);
		((ConfigurableApplicationContext) context).close();
		return map;
	}

	// 退出
	@RequestMapping(value = "/logout")
	public String Logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return "login";
	}

	// 返回show页面
	@RequestMapping(value = "/show")
	public String show(HttpServletRequest request, ModelMap map) throws UnsupportedEncodingException {
		Product product = new Product();
		product.setId(Integer.valueOf(request.getParameter("id")));
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-mybatis.xml");
		ContentDao dao = context.getBean("contentDao", ContentDao.class);
		Content content = dao.getContentById(product.getId());
		product.setImage(content.getIcon());
		product.setTitle(content.getTitle());
		product.setSummary(content.getAbs());
		product.setBuyNum(1); 								///////////////////// test
		if (content.getText() != null)
			product.setDetail(content.getText());
		product.setPrice(content.getPrice());

		HttpSession session = request.getSession();
		if (session.getAttribute("userName") != null) {
			Person user = new Person();
			user.setUsername("user");
			user.setUsertype((Integer) session.getAttribute("userType"));
			map.addAttribute("user", user);
			TrxDao dao2 = context.getBean("trxDao", TrxDao.class);

			Trx trx = dao2.geTrxByContentId(product.getId()); //
			if (trx != null) {
				product.setBuyPrice(trx.getPrice());
				product.setIsBuy(true);
				product.setIsSell(true);
			}
		}
		
		map.addAttribute("total",100);
		map.addAttribute("product", product);
		((ConfigurableApplicationContext) context).close();
		return "show";
	}

	// 返回public页面
	@RequestMapping(value = "/public")
	public String returnPublic(HttpServletRequest request, ModelMap map) {
		HttpSession session = request.getSession();
		if (session.getAttribute("userName") != null) {
			Person user = new Person();
			user.setUsername((String) session.getAttribute("userName"));
			user.setUsertype((Integer) session.getAttribute("userType"));
			map.addAttribute("user", user);
		}
		return "public";
	}

	// 返回publicSubmit页面
	@RequestMapping(value = "/publicSubmit", method = POST)
	public String publicSubmit(@RequestParam("title") String title, @RequestParam("image") String image,
			@RequestParam("detail") String detail, @RequestParam("price") double price,
			@RequestParam("summary") String summary, ModelMap map, HttpServletRequest request)
			throws UnsupportedEncodingException {
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-mybatis.xml");
		ContentDao contentDao = context.getBean("contentDao", ContentDao.class);

		contentDao.insert((int) (price * 100), title, image, summary, detail);

		Product product = new Product();
		Content content = contentDao.getContentByTitle(title);
		product.setId(content.getId());
		product.setImage(content.getIcon());
		product.setTitle(content.getTitle());
		product.setSummary(content.getAbs());
		if (content.getText() != null)
			product.setDetail(content.getText());
		product.setPrice(content.getPrice());
		map.addAttribute("product", product);

		HttpSession session = request.getSession();
		if (session.getAttribute("userName") != null) {
			Person user = new Person();
			user.setUsername("user");
			user.setUsertype((Integer) session.getAttribute("userType"));
			map.addAttribute("user", user);
		}

		((ConfigurableApplicationContext) context).close();
		return "publicSubmit";
	}

	// 返回edit页面
	@RequestMapping(value = "/edit")
	public String edit(HttpServletRequest request, @RequestParam("id") int id, ModelMap map)
			throws UnsupportedEncodingException {

		ApplicationContext context = new ClassPathXmlApplicationContext("spring-mybatis.xml");
		ContentDao contentDao = context.getBean("contentDao", ContentDao.class);

		Product product = new Product();
		Content content = contentDao.getContentById(id);
		product.setId(content.getId());
		product.setImage(content.getIcon());
		product.setTitle(content.getTitle());
		product.setSummary(content.getAbs());
		product.setDetail(content.getText());
		product.setPrice(content.getPrice());
		map.addAttribute("product", product);

		HttpSession session = request.getSession();
		if (session.getAttribute("userName") != null) {
			Person user = new Person();
			user.setUsername("user");
			user.setUsertype((Integer) session.getAttribute("userType"));
			map.addAttribute("user", user);
		}
		((ConfigurableApplicationContext) context).close();
		return "edit";
	}

	// 上传图片
	@RequestMapping(value = "/api/upload", method = POST)
	public void upload(@RequestPart("file") Part profilePicture) throws IOException {
		profilePicture.write("/data/" + profilePicture.getName());
	}

	@RequestMapping(value = "/settleAccount", method = GET)
	public String settleAccount(HttpServletRequest request, ModelMap map) {
		HttpSession session = request.getSession();
		if (session.getAttribute("userName") != null) {
			Person user = new Person();
			user.setUsername((String) session.getAttribute("userName"));
			user.setUsertype((Integer) session.getAttribute("userType"));
			map.addAttribute("user", user);
		}
		return "settleAccount";
	}

	// 删除商品
	@RequestMapping(value = "/api/delete", method = POST)
	public ModelMap Delete(@RequestParam("id") int id) {
		ModelMap map = new ModelMap();

		ApplicationContext context = new ClassPathXmlApplicationContext("spring-mybatis.xml");
		TrxDao trxDao = context.getBean("trxDao", TrxDao.class);
		if (trxDao.geTrxByContentId(id) == null) {
			ContentDao contentDao = context.getBean("contentDao", ContentDao.class);
			contentDao.deleteById(id);
			map.addAttribute("code", 200);
			map.addAttribute("message", "OK");
			map.addAttribute("result", true);
			((ConfigurableApplicationContext) context).close();
			return map;
		} else {
			map.addAttribute("code", 202);
			map.addAttribute("message", "删除失败");
			map.addAttribute("result", true);
			((ConfigurableApplicationContext) context).close();
			return map;
		}
	}

	// 返回editSubmit页面
	@RequestMapping(value = "/editSubmit")
	public String editSubmit(@RequestParam("title") String title, @RequestParam("image") String image,
			@RequestParam("detail") String detail, @RequestParam("price") double price,
			@RequestParam("summary") String summary, @RequestParam("id") int id, HttpServletRequest request,
			ModelMap map) throws UnsupportedEncodingException {
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-mybatis.xml");
		ContentDao contentDao = context.getBean("contentDao", ContentDao.class);

		contentDao.update(id, (int) (price * 100), title, image, summary, detail);

		Product product = new Product();
		Content content = contentDao.getContentById(id);
		product.setId(content.getId());
		product.setImage(content.getIcon());
		product.setTitle(content.getTitle());
		product.setSummary(content.getAbs());
		if (content.getText() != null)
			product.setDetail(content.getText());
		product.setPrice(content.getPrice());
		map.addAttribute("product", product);

		HttpSession session = request.getSession();
		if (session.getAttribute("userName") != null) {
			Person user = new Person();
			user.setUsername("user");
			user.setUsertype((Integer) session.getAttribute("userType"));
			map.addAttribute("user", user);
		}

		((ConfigurableApplicationContext) context).close();
		return "editSubmit";
	}
	
	@RequestMapping(value = "/api/buy", method = POST)
	public ModelMap Buy(@RequestBody List<BuyList> buyList,
			HttpServletRequest request) {
		ModelMap map = new ModelMap();
		
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-mybatis.xml");
		TrxDao trxDao = context.getBean("trxDao", TrxDao.class);
		ContentDao contentDao = context.getBean("contentDao", ContentDao.class);
		PersonDao personDao = context.getBean("personDao", PersonDao.class);
		
		if(buyList != null){
			for(BuyList x : buyList){
				Date date = new Date();
				for(int i = 0; i < x.getNumber(); i++){
					int contentId = x.getId();
					int price = contentDao.getPriceById(contentId);
					int personId = personDao.getPersonId((String)request.getSession().getAttribute("userName"));
					trxDao.insert(contentId, personId, price,date.getTime());
				}
			}
			map.addAttribute("code", 200);
			map.addAttribute("message", "OK");
			map.addAttribute("result", true);
		}
		else{
			map.addAttribute("code", 202);
			map.addAttribute("message", "购买失败");
			map.addAttribute("result", true);
		}
		((ConfigurableApplicationContext) context).close();
		return map;
	}
	
	// 返回account页面
	@RequestMapping(value = "/account", method=GET)
	public String account(HttpServletRequest request, ModelMap map) {
		
		HttpSession session = request.getSession();
		String userName = (String) session.getAttribute("userName");
		
		if (session.getAttribute("userName")!=null) {
			Person user = new Person();
			user.setUsername("user");
			user.setUsertype((Integer) session.getAttribute("userType"));
			map.addAttribute("user", user);
		}
		
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-mybatis.xml");
		
		PersonDao personDao = context.getBean("personDao",PersonDao.class);
		TrxDao trxDao = context.getBean("trxDao", TrxDao.class);
		ContentDao contentDao = context.getBean("contentDao", ContentDao.class);
		
		int personId = personDao.getPersonId(userName);
		List<Content> contents = contentDao.getContentList();
		List<Trx> trxs = trxDao.getTrxListById(personId);	
		List<Product> buyList = new ArrayList<Product>();	
		
		for(Trx trx : trxs){
			Product p = new Product();
			p.setBuyPrice(trx.getPrice());
			p.setBuyTime(trx.getTime());
			p.setId(trx.getContentId());
			p.setBuyNum(10);			////////////测试
			for(Content content : contents){
				if (content.getId() == p.getId()) {
					p.setImage(content.getIcon());
					p.setTitle(content.getTitle());
				}
			}
			buyList.add(p);
		}
		map.addAttribute("buyList", buyList);
		((ConfigurableApplicationContext) context).close();
		return "account";
	}
	
}
