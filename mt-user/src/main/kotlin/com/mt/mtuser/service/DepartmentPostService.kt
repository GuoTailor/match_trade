package com.mt.mtuser.service

import com.mt.mtuser.dao.department.DepartmentDao
import com.mt.mtuser.dao.department.DepartmentPostDao
import com.mt.mtuser.dao.department.PostDao
import com.mt.mtuser.entity.department.Department
import com.mt.mtuser.entity.department.DepartmentPost
import com.mt.mtuser.entity.department.DepartmentPostInfo
import com.mt.mtuser.entity.department.Post
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.r2dbc.core.flow
import org.springframework.stereotype.Service
import java.util.*

/**
 * Created by gyh on 2020/7/20
 */
@Service
class DepartmentPostService {
    @Autowired
    lateinit var departmentDao: DepartmentDao

    @Autowired
    lateinit var postDao: PostDao

    @Autowired
    lateinit var departmentPostDao: DepartmentPostDao

    @Autowired
    protected lateinit var connect: DatabaseClient

    @Autowired
    lateinit var roleService: RoleService
    private val bindMutex = Mutex()


    suspend fun bindDepartment(dpi: DepartmentPostInfo): DepartmentPost {
        bindMutex.withLock {
            val department = departmentDao.findByName(dpi.departmentName!!)
                    ?: departmentDao.save(Department(name = dpi.departmentName))
            val post = dpi.postName?.let { postDao.findByName(it) ?: postDao.save(Post(name = it)) }
            return exitRelated(department.id!!, post?.id, dpi.companyId!!)
                    ?: departmentPostDao.save(DepartmentPost(departmentId = department.id, postId = post?.id, companyId = dpi.companyId))
        }
    }

    suspend fun updateBind(dpi: DepartmentPostInfo): DepartmentPost {
        bindMutex.withLock {
            val department = departmentDao.findByName(dpi.departmentName!!)
                    ?: departmentDao.save(Department(name = dpi.departmentName))
            val post = dpi.postName?.let { postDao.findByName(it) ?: postDao.save(Post(name = it)) }
            return exitRelated(department.id!!, post?.id, dpi.companyId!!)
                    ?: departmentPostDao.save(DepartmentPost(id = dpi.id, departmentId = department.id, postId = post?.id, companyId = dpi.companyId))
        }
    }

    suspend fun deleteBind(id: Int) {
        if (roleService.existsByDpId(id) > 0) {
            error("该部门下存在用户")
        } else {
            departmentPostDao.deleteById(id)
        }
    }

    suspend fun findAllBind(companyId: Int): LinkedList<Department> {
        val dataList = connect.execute("select mdp.id, md.name as dname, mp.name as pname " +
                " from mt_department_post mdp " +
                " left join mt_department md on mdp.department_id = md.id " +
                " left join mt_post mp on mdp.post_id = mp.id " +
                " where company_id = :companyId ")
                .bind("companyId", companyId)
                .map { r, _ ->
                    val dpi = DepartmentPostInfo()
                    dpi.id = r.get("id", java.lang.Integer::class.java)?.toInt()
                    dpi.departmentName = r.get("dname", String::class.java)
                    dpi.postName = r.get("pname", String::class.java)
                    dpi
                }.flow().toList()
        val list = LinkedList<Department>()
        dataList.forEach {
            val department = list.find { d -> d.name == it.departmentName }
                    ?: Department(id = it.id, name = it.departmentName)
            val post = Post(name = it.postName)
            department.postList.add(post)
            if (!list.contains(department)) {
                list.add(department)
            }
        }
        return list
    }

    suspend fun exitRelated(departmentId: Int, postId: Int?, companyId: Int): DepartmentPost? {
        val subSql = if (postId == null) "post_id is null" else "post_id = $postId"
        return connect.execute("select * from mt_department_post where company_id = :companyId and department_id = :departmentId and $subSql limit 1")
                .bind("companyId", companyId)
                .bind("departmentId", departmentId)
                .`as`(DepartmentPost::class.java)
                .fetch()
                .awaitOneOrNull()
    }

}